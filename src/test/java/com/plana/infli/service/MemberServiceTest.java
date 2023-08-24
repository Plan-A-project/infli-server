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
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
