package com.plana.infli.service;

import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
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

    @DisplayName("학생 회원으로 회원가입 성공")
    @Test
    void signupAsStudentMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        CreateStudentMemberServiceRequest request = CreateStudentMemberServiceRequest.builder()
                .username("jin1234")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin1234")
                .universityId(university.getId())
                .build();

        //when
        Long memberId = memberService.signupAsStudentMember(request);

        //then
        assertThat(memberRepository.count()).isEqualTo(1);
        Member findMember = memberRepository.findAll().get(0);
        assertThat(findMember.getId()).isEqualTo(memberId);
        assertThat(findMember.getLoginCredentials().getUsername()).isEqualTo("jin1234");
        assertThat(findMember.getStudentCredentials().getRealName()).isEqualTo("이영진");
        assertThat(findMember.getRole()).isEqualTo(STUDENT);
        assertThat(findMember.getBasicCredentials().getNickname()).isEqualTo("jin1234");
        assertThat(encoder.matches("password1234!",
                findMember.getLoginCredentials().getPassword())).isTrue();
    }

    @DisplayName("학생 회원으로 회원가입 실패 - 비밀번호와 비밀번호 확인이 일치하지 않은 경우")
    @Test
    void signupAsStudentMemberPasswordAndPasswordConfirmNotMatch() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        CreateStudentMemberServiceRequest request = CreateStudentMemberServiceRequest.builder()
                .username("jin1234")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("123456")
                .nickname("jin1234")
                .universityId(university.getId())
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.signupAsStudentMember(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    @DisplayName("학생 회원으로 회원가입 실패 - 대학이 존재하지 않는 경우")
    @Test
    void signupAsStudentMemberNotExistingUniversity() {
        //given
        CreateStudentMemberServiceRequest request = CreateStudentMemberServiceRequest.builder()
                .username("jin1234")
                .realName("이영진")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .nickname("jin1234")
                .universityId(-1L)
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.signupAsStudentMember(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("대학교가 존재하지 않습니다");
    }

    @DisplayName("기업 회원으로 회원가입 성공")
    @Test
    void signupAsCompanyMember() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        CreateCompanyMemberServiceRequest request = CreateCompanyMemberServiceRequest.builder()
                .username("jin1234")
                .nickname("jin1234")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        //when
        Long memberId = memberService.signupAsCompanyMember(request);

        //then
        assertThat(memberRepository.count()).isEqualTo(1);
        Member findMember = memberRepository.findActiveMemberWithCompanyBy("jin1234").get();
        assertThat(findMember.getId()).isEqualTo(memberId);
        assertThat(findMember.getLoginCredentials().getUsername()).isEqualTo("jin1234");
        assertThat(findMember.getRole()).isEqualTo(COMPANY);
        assertThat(findMember.getBasicCredentials().getNickname()).isEqualTo("jin1234");
        assertThat(encoder.matches("password1234!",
                findMember.getLoginCredentials().getPassword())).isTrue();
        assertThat(findMember.getCompanyCredentials().getCompany().getName()).isEqualTo("카카오");
    }

    @DisplayName("기업 회원으로 회원가입 실패 - 비밀번호와 비밀번호 확인이 일치하지 않는 경우")
    @Test
    void signupAsCompanyMemberPasswordAndPasswordConfirmNotMatch() {
        //given
        University university = universityFactory.createUniversity("푸단대학교");
        CreateCompanyMemberServiceRequest request = CreateCompanyMemberServiceRequest.builder()
                .username("jin1234")
                .nickname("jin1234")
                .password("password1234!")
                .passwordConfirm("123456")
                .universityId(university.getId())
                .companyName("카카오")
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.signupAsCompanyMember(request))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    @DisplayName("기업 회원으로 회원가입 실패 - 대학이 존재하지 않는 경우")
    @Test
    void signupAsCompanyMemberUniversityNotExist() {
        //given
        CreateCompanyMemberServiceRequest request = CreateCompanyMemberServiceRequest.builder()
                .username("jin1234")
                .nickname("jin1234")
                .password("password1234!")
                .passwordConfirm("password1234!")
                .universityId(-1L)
                .companyName("카카오")
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.signupAsCompanyMember(request))
                .isInstanceOf(NotFoundException.class)
                .message().isEqualTo("대학교가 존재하지 않습니다");
    }


    @DisplayName("회원가입시 사용 가능한 닉네임인지 확인 - 사용 가능한 경우")
    @Test
    void checkIfValidNickname() {
        //when
        boolean isValidNickname = memberService.checkIsValidNickname("jin1234");

        //then
        assertThat(isValidNickname).isTrue();
    }

    @DisplayName("회원가입시 사용 가능한 닉네임인지 확인 - 닉네임 규칙에 맞지 않는 경우")
    @Test
    void checkIfValidNicknameInvalidRegex() {
        //when //then
        assertThatThrownBy(() -> memberService.checkIsValidNickname("!!!!!!!!!!!!!!!!"))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요");
    }

    @DisplayName("회원가입시 사용 가능한 닉네임인지 확인 - 이미 사용중인 닉네임인 경우")
    @Test
    void checkIfValidNicknameAlreadyInUse() {
        //when
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when //then
        assertThatThrownBy(() -> memberService.checkIsValidNickname(
                member.getBasicCredentials().getNickname()))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 존재하는 닉네임입니다.");
    }

    @DisplayName("회원가입시 사용 가능한 username인지 확인 - 사용 가능한 경우")
    @Test
    void checkIfValidUsername() {
        //when
        boolean isValidNickname = memberService.checkIsValidUsername("jin1234");

        //then
        assertThat(isValidNickname).isTrue();
    }

    @DisplayName("회원가입시 사용 가능한 username인지 확인 - username 규칙에 맞지 않는 경우")
    @Test
    void checkIfValidUsernameInvalidRegex() {
        //when //then
        assertThatThrownBy(
                () -> memberService.checkIsValidUsername("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))
                .isInstanceOf(BadRequestException.class)
                .message().isEqualTo("영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요");
    }

    @DisplayName("회원가입시 사용 가능한 username인지 확인 - 이미 사용중인 username인 경우")
    @Test
    void checkIfValidUsernameAlreadyInUse() {
        //when
        University university = universityFactory.createUniversity("푸단대학교");
        Member member = memberFactory.createVerifiedStudentMember("nickname", university);

        //when //then
        assertThatThrownBy(
                () -> memberService.checkIsValidUsername(
                        member.getLoginCredentials().getUsername()))
                .isInstanceOf(ConflictException.class)
                .message().isEqualTo("이미 사용중인 ID 입니다");
    }
}
