package com.plana.infli.service.mock;

import static com.plana.infli.domain.EmailVerification.*;
import static com.plana.infli.domain.embedded.member.BasicCredentials.ofDefaultWithNickname;
import static com.plana.infli.domain.embedded.member.ProfileImage.ofDefaultProfileImage;
import static com.plana.infli.domain.type.Role.COMPANY;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.VerificationStatus.NOT_STARTED;
import static com.plana.infli.domain.type.VerificationStatus.PENDING;
import static java.time.LocalDateTime.*;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.EmailVerification;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.CompanyCredentials;
import com.plana.infli.domain.embedded.member.LoginCredentials;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.infra.exception.custom.InternalServerErrorException;
import com.plana.infli.repository.emailVerification.EmailVerificationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.service.MemberService;
import com.plana.infli.service.util.S3Uploader;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.ion.IonException;

@ExtendWith(MockitoExtension.class)
class MemberServiceMockTest {

    @Spy
    private PasswordEncoder encoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private SendGrid sendGrid;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private MemberService memberService;


    @DisplayName("대학교 인증 이메일 발송 성공")
    @Test
    void sendUniversityVerificationEmail() throws Exception {
        //given
        University university = University.create("푸단대학교");
        Member member = createStudentMember(university);
        EmailVerification emailVerification = create(member, now(), "jin123@fudan.edu.cn");

        given(sendGrid.api(any(Request.class))).willReturn(
                new Response(SC_ACCEPTED, "Success", null));

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(Optional.of(member));

        given(emailVerificationRepository.save(any(EmailVerification.class)))
                .willReturn(emailVerification);

        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .universityEmail("jin123@fudan.edu.cn")
                .build();

        //when
        memberService.sendVerificationMail(request);

        //then
        assertThat(member.getVerificationStatus()).isEqualTo(PENDING);
        assertThat(member.getStudentCredentials().getUniversityEmail()).isEqualTo(
                "jin123@fudan.edu.cn");
    }

    @DisplayName("대학교 인증 이메일 발송 실패")
    @Test
    void sendUniversityVerificationEmailFail() throws Exception {
        //given
        University university = University.create("푸단대학교");
        Member member = createStudentMember(university);
        EmailVerification emailVerification = create(member, now(), "jin123@fudan.edu.cn");

        given(sendGrid.api(any(Request.class))).willReturn(
                new Response(SC_UNAUTHORIZED, "FAIL", null));

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(Optional.of(member));

        given(emailVerificationRepository.save(any(EmailVerification.class)))
                .willReturn(emailVerification);

        SendVerificationMailServiceRequest request = SendVerificationMailServiceRequest.builder()
                .username(member.getLoginCredentials().getUsername())
                .universityEmail("jin123@fudan.edu.cn")
                .build();

        //when //then
        assertThatThrownBy(() -> memberService.sendVerificationMail(request))
                .isInstanceOf(InternalServerErrorException.class)
                .message().isEqualTo("이메일 전송에 실패했습니다");
    }

    @DisplayName("특정 회원이 학생 인증을 받기 위해 본인의 대학교 재학 증명서를 업로드")
    @Test
    void uploadUniversityCertificateImage() {
        //given
        University university = University.create("푸단대학교");
        Member member = createStudentMember(university);

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(Optional.of(member));

        given(s3Uploader.uploadAsOriginalImage(any(MultipartFile.class), anyString()))
                .willReturn("https://s3.aws.mockImage.png");

        MockMultipartFile file = new MockMultipartFile("mockImage", new byte[]{1, 2, 3, 4,});


        //when
        memberService.uploadUniversityCertificateImage(member.getUniversity().getName(), file);

        //then
        assertThat(member.getStudentCredentials().getUniversityCertificateUrl())
                .isEqualTo("https://s3.aws.mockImage.png");
        assertThat(member.getVerificationStatus()).isEqualTo(PENDING);
    }

    @DisplayName("특정 회원이 기업 인증을 받기 위해 해당 회원이 소속된 회사의 사업자 등록증을 업로드")
    @Test
    void uploadCompanyCertificateImage() {

        //given
        University university = University.create("푸단대학교");
        Member member = createCompanyMember(university);

        given(memberRepository.findActiveMemberBy(anyString())).willReturn(Optional.of(member));

        given(s3Uploader.uploadAsOriginalImage(any(MultipartFile.class), anyString()))
                .willReturn("https://s3.aws.CompanyCertificate.png");

        MockMultipartFile file = new MockMultipartFile("mockImage", new byte[]{1, 2, 3, 4,});


        //when
        memberService.uploadCompanyCertificateImage(member.getUniversity().getName(), file);

        //then
        assertThat(member.getCompanyCredentials().getCompanyCertificateUrl())
                .isEqualTo("https://s3.aws.CompanyCertificate.png");
        assertThat(member.getVerificationStatus()).isEqualTo(PENDING);
    }


    Member createStudentMember(University university) {
        return Member.builder()
                .university(university)
                .role(STUDENT)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(LoginCredentials.of(randomUUID().toString(),
                        encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(UUID.randomUUID().toString()))
                .companyCredentials(null)
                .studentCredentials(StudentCredentials.ofDefault("이영진"))
                .build();
    }

    Member createCompanyMember(University university) {

        Company company = Company.create("카카오");

        return Member.builder()
                .university(university)
                .role(COMPANY)
                .verificationStatus(NOT_STARTED)
                .loginCredentials(LoginCredentials.of(randomUUID().toString(),
                        encoder.encode("password")))
                .profileImage(ofDefaultProfileImage())
                .basicCredentials(ofDefaultWithNickname(UUID.randomUUID().toString()))
                .companyCredentials(CompanyCredentials.ofDefault(company))
                .studentCredentials(null)
                .build();
    }
}
