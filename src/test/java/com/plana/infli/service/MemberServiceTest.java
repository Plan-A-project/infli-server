package com.plana.infli.service;

import static com.plana.infli.domain.embedded.member.StudentCredentials.ofDefault;
import static com.plana.infli.domain.embedded.member.StudentCredentials.ofWithEmail;
import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static com.plana.infli.domain.type.VerificationStatus.SUCCESS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private PasswordEncoder encoder;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }

    public static Stream<Arguments> providingRoleForCheckingMemberAcceptedWritePolicy() {
        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의 안한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void False_checkMemberAcceptedWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyNotAcceptedMemberWithRole(university, role);

        //when
        boolean agreedOnWritePolicy = memberService.checkMemberAcceptedPolicy(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(agreedOnWritePolicy).isFalse();
    }

    @DisplayName("글 작성 규정 동의 여부 확인 - 동의한 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForCheckingMemberAcceptedWritePolicy")
    void True_checkMemberAcceptedWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyAcceptedMemberWithRole(university, role);

        //when
        boolean agreedOnWritePolicy = memberService.checkMemberAcceptedPolicy(
                member.getLoginCredentials().getUsername());

        //then
        assertThat(agreedOnWritePolicy).isTrue();
    }

    public static Stream<Arguments> providingRoleForMemberAcceptingWritePolicy() {
        return Stream.of(
                Arguments.of(STUDENT),
                Arguments.of(COMPANY),
                Arguments.of(STUDENT_COUNCIL),
                Arguments.of(ADMIN)
        );
    }

    @DisplayName("글 작성 규정 동의함 요청 성공")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Success_AcceptingWritePolicy(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyNotAcceptedMemberWithRole(university, role);

        //when
        memberService.acceptPolicy(member.getLoginCredentials().getUsername());

        //then
        Member findMember = memberRepository.findActiveMemberBy(
                member.getLoginCredentials().getUsername()).get();
        assertThat(findMember.getBasicCredentials().isPolicyAccepted()).isTrue();
    }

    @DisplayName("글 작성 규정 동의함 요청 실패 - 해당 회원이 탈퇴 회원인 경우")
    @ParameterizedTest(name = "{index} 회원 유형: {0}")
    @MethodSource("providingRoleForMemberAcceptingWritePolicy")
    void Fail_AcceptingWritePolicyByDeletedMember(Role role) {

        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createPolicyNotAcceptedMemberWithRole(university, role);
        memberRepository.delete(member);

        //when //then
        assertThatThrownBy(() -> memberService.acceptPolicy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }

    @DisplayName("글 작성 규정 동의함 요청 실패 - 회원이 존재하지 않는 경우")
    @Test
    void Fail_AcceptingWritePolicyByNotExistingMember() {
        //when //then
        assertThatThrownBy(() -> memberService.acceptPolicy("aaa"))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("사용자를 찾을수 없습니다");
    }


    @DisplayName("이메일 인증 메일 발송 실패 - 입력한 이메일이 대학교 이메일이 아닌 경우")
    @Test
    void sendVerificationMailToNonUniversityEmail() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createUnverifiedStudentMember("member", university);
        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest
                .builder()
                .universityEmail("1234@naver.com")
                .username(member.getLoginCredentials().getUsername())
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.sendVerificationMail(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("유효하지 않은 대학 이메일 주소 입니다");
    }

    @DisplayName("이메일 인증 메일 발송 실패 - 이미 이메일 인증을 받은 회원인 경우")
    @Test
    void sendVerificationMailToVerifiedMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("member", university);
        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest
                .builder()
                .universityEmail("1234@fudan.edu.cn")
                .username(member.getLoginCredentials().getUsername())
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.sendVerificationMail(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("이미 대학교 이메일 인증을 완료했습니다");
    }

    @DisplayName("이메일 인증 메일 발송 실패 - 입력한 이메일로 이미 인증을 받은 회원이 존재하는 경우")
    @Test
    void emailVerificationDuplicate() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        Member verifiedMember = createVerifiedStudentMemberWithEmail(
                "1234@fudan.edu.cn", "member", university);

        Member member = memberFactory.createUnverifiedStudentMember("nickname", university);
        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest
                .builder()
                .universityEmail("1234@fudan.edu.cn")
                .username(member.getLoginCredentials().getUsername())
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.sendVerificationMail(request))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 사용중인 대학교 이메일 입니다");
    }

    Member createVerifiedStudentMemberWithEmail(String email, String nickname, University university) {
        return memberRepository.save(Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(SUCCESS)
                .loginCredentials(LoginCredentials.of(
                        randomUUID().toString().substring(0, 10), "password1234!"))
                .profileImage(ProfileImage.ofDefaultProfileImage())
                .basicCredentials(BasicCredentials.ofDefaultWithNickname(nickname))
                .companyCredentials(null)
                .studentCredentials(ofWithEmail(ofDefault("이영진"), email))
                .build());
    }
}
