package com.plana.infli.service;

import static com.plana.infli.domain.embedded.member.StudentCredentials.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.util.UUID.*;
import static org.assertj.core.api.Assertions.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.factory.MemberFactory;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.repository.emailVerification.EmailVerificationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberFactory memberFactory;

    @AfterEach
    void tearDown() {
        emailVerificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
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
        assertThatThrownBy(() -> mailService.sendVerificationMail(request))
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
        assertThatThrownBy(() -> mailService.sendVerificationMail(request))
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
        assertThatThrownBy(() -> mailService.sendVerificationMail(request))
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
