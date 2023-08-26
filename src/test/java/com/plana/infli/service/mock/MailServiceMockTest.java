//package com.plana.infli.service.mock;
//
//import static com.plana.infli.domain.embedded.member.BasicCredentials.ofDefaultWithNickname;
//import static com.plana.infli.domain.embedded.member.ProfileImage.ofDefaultProfileImage;
//import static com.plana.infli.domain.type.Role.STUDENT;
//import static com.plana.infli.domain.type.VerificationStatus.NOT_STARTED;
//import static com.plana.infli.domain.type.VerificationStatus.PENDING;
//import static java.util.UUID.randomUUID;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
//
//import com.plana.infli.domain.Member;
//import com.plana.infli.domain.University;
//import com.plana.infli.domain.embedded.member.LoginCredentials;
//import com.plana.infli.domain.embedded.member.StudentCredentials;
//import com.plana.infli.factory.MemberFactory;
//import com.plana.infli.factory.UniversityFactory;
//import com.plana.infli.repository.member.MemberRepository;
//import com.plana.infli.service.MailService;
//import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
//import com.sendgrid.helpers.mail.Mail;
//import java.lang.reflect.Method;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@ExtendWith(MockitoExtension.class)
//public class MailServiceMockTest {
//
//    @Spy
//    private MemberFactory memberFactory;
//
//    @Spy
//    private MemberRepository memberRepository;
//
//    @Spy
//    private UniversityFactory universityFactory;
//
//    @Spy
//    private PasswordEncoder encoder;
//
//    @Spy
//    private MailService mailService;
//
//    @DisplayName("대학교 인증 이메일 발송 성공")
//    @Test
//    void sendUniversityVerificationEmail() throws Exception {
//        //given
//        University university = universityFactory.createUniversity("푸단대학교");
//        Member member = memberFactory.createUncertifiedStudentMember("member", university);
//
//        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest.builder()
//                .username(member.getLoginCredentials().getUsername())
//                .universityEmail("jin123@fudan.edu.cn")
//                .build();
//
//        Method sendMail = MailService.class.getDeclaredMethod("sendMail", Mail.class);
//        sendMail.setAccessible(true);
//        doNothing().when(sendMail)
//                .invoke(mailService, any(Mail.class));
//
//        //when
//        mailService.sendVerificationMail(request);
//
//        //then
//        Member findMember = memberRepository.findActiveMemberBy(member.getId()).get();
//        assertThat(findMember.getStudentCredentials().getUniversityEmail())
//                .isEqualTo("jin123@fudan.edu.cn");
//        assertThat(findMember.getVerificationStatus()).isEqualTo(PENDING);
//    }
//
//    Member createStudentMember(University university) {
//
//        return Member.builder()
//                .university(university)
//                .role(STUDENT)
//                .verificationStatus(NOT_STARTED)
//                .loginCredentials(LoginCredentials.of(randomUUID().toString(),
//                        encoder.encode("password")))
//                .profileImage(ofDefaultProfileImage())
//                .basicCredentials(ofDefaultWithNickname(UUID.randomUUID().toString()))
//                .companyCredentials(null)
//                .studentCredentials(StudentCredentials.ofDefault("이영진"))
//                .build();
//    }
//}
