package com.plana.infli.service;

import static com.plana.infli.domain.Company.*;
import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.COMPANY_VERIFICATION_ALREADY_EXISTS;
import static com.plana.infli.infra.exception.custom.BadRequestException.EMAIL_VERIFICATION_ALREADY_EXISTS;
import static com.plana.infli.infra.exception.custom.BadRequestException.EMAIL_VERIFICATION_CODE_EXPIRED;
import static com.plana.infli.infra.exception.custom.BadRequestException.IMAGE_IS_EMPTY;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_EMAIL_CODE;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_NICKNAME;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_STUDENT_VERIFICATION_REQUEST;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_UNIVERSITY_EMAIL;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_USERNAME;
import static com.plana.infli.infra.exception.custom.BadRequestException.NOT_MATCHES_PASSWORD_CONFIRM;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_NICKNAME;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_UNIVERSITY_EMAIL;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_USERNAME;
import static com.plana.infli.infra.exception.custom.InternalServerErrorException.EMAIL_SEND_FAILED;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.UNIVERSITY_NOT_FOUND;
import static com.sendgrid.Method.POST;
import static jakarta.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static java.time.LocalDateTime.now;
import static java.util.List.*;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.EmailVerification;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.editor.MemberEditor;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.InternalServerErrorException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.emailVerification.EmailVerificationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.aop.upload.Upload;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
import com.plana.infli.web.dto.response.member.verification.VerificationStatusResponse;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    public static final String USERNAME_REGEX = "^[a-z0-9_-]{5,20}$";

    public static final String REAL_NAME_REGEX = "^[가-힣]{2,10}$";

    public static final String NICKNAME_REGEX = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,8}$";

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$";

    private static final String MESSAGE_FROM = "no-reply@infli.co";

    private static final List<String> ALLOWED_EMAIL_SUFFIX = of("@fudan.edu.cn", "@m.fudan.edu.cn");

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    private final CompanyRepository companyRepository;

    private final EmailVerificationRepository emailVerificationRepository;

    private final PasswordEncoder encoder;

    private final S3Uploader s3Uploader;

    private final SendGrid sendGrid;

    @Transactional
    public Long signupAsStudentMember(CreateStudentMemberServiceRequest request) {

        validateCreateStudentMemberRequest(request);

        University university = findUniversityBy(request.getUniversityId());

        Member member = request.toEntity(university, encodePassword(request.getPassword()));

        return memberRepository.save(member).getId();
    }

    private String encodePassword(String password) {
        return encoder.encode(password);
    }

    private void validateCreateStudentMemberRequest(CreateStudentMemberServiceRequest request) {

        checkPasswordConfirmMatch(request.getPassword(), request.getPasswordConfirm());
        checkIsValidUsername(request.getUsername());
        checkIsValidNickname(request.getNickname());
    }

    private void checkPasswordConfirmMatch(String password, String passwordConfirm) {
        if (password.equals(passwordConfirm) == false) {
            throw new BadRequestException(NOT_MATCHES_PASSWORD_CONFIRM);
        }
    }

    public boolean checkIsValidUsername(String username) {
        if (username.matches(USERNAME_REGEX) == false) {
            throw new BadRequestException(INVALID_USERNAME);
        }

        if (memberRepository.existsByUsername(username)) {
            throw new ConflictException(DUPLICATED_USERNAME);
        }

        return true;
    }

    public boolean checkIsValidNickname(String nickname) {
        if (nickname.matches(NICKNAME_REGEX) == false) {
            throw new BadRequestException(INVALID_NICKNAME);
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }

        return true;
    }

    private University findUniversityBy(Long universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
    }

    @Transactional
    public Long signupAsCompanyMember(CreateCompanyMemberServiceRequest request) {

        validateCreateCompanyMemberRequest(request);

        Company company = findCompanyOrCreateBy(request.getCompanyName());

        University university = findUniversityBy(request.getUniversityId());

        Member member = request.toEntity(company, encodePassword(request.getPassword()),
                university);

        return memberRepository.save(member).getId();
    }

    private Company findCompanyOrCreateBy(String companyName) {
        return companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(create(companyName)));
    }

    private void validateCreateCompanyMemberRequest(CreateCompanyMemberServiceRequest request) {
        checkPasswordConfirmMatch(request.getPassword(), request.getPasswordConfirm());
        checkIsValidUsername(request.getUsername());
    }

    @Transactional
    public void uploadCompanyCertificateImage(String username, MultipartFile file) {
        Member member = findMemberBy(username);

        validateUploadCompanyCertificateRequest(member, file);

        String directoryPath = "members/" + member.getId() + "/certificate/company";

        String imageUrl = s3Uploader.uploadAsOriginalImage(file, directoryPath);

        setVerificationStatusAsPendingByCompanyCertificate(member, imageUrl);
    }

    private void validateUploadCompanyCertificateRequest(Member member, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(COMPANY_VERIFICATION_ALREADY_EXISTS);
        }
    }

    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public void uploadUniversityCertificateImage(String username, MultipartFile file) {

        Member member = findMemberBy(username);

        validateUploadUniversityCertificateRequest(member, file);

        String directoryPath = "members/" + member.getId() + "/certificate/student";

        String imageUrl = s3Uploader.uploadAsOriginalImage(file, directoryPath);

        setVerificationStatusAsPendingByUniversityCertificate(member, imageUrl);
    }

    private void validateUploadUniversityCertificateRequest(Member member, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(COMPANY_VERIFICATION_ALREADY_EXISTS);
        }
    }

    public boolean checkMemberAcceptedPolicy(String username) {
        BasicCredentials basicCredentials = findMemberBy(username).getBasicCredentials();

        return basicCredentials.isPolicyAccepted();
    }

    @Transactional
    public void acceptPolicy(String username) {
        Member member = findMemberBy(username);

        MemberEditor.acceptPolicy(member);
    }

    public VerificationStatusResponse loadVerificationStatus(String username) {
        Member member = findMemberBy(username);

        return VerificationStatusResponse.of(member);
    }



    // TODO 탈퇴후 동일한 대학 이메일로 가입하려는 경우 고려해야함
    // TODO 학생 회원만 업로드 가능한지 검증 필요
    // TODO AOp 다시 확인 해야됨
    @Upload
    @Transactional
    public void sendVerificationMail(SendVerificationMailServiceRequest request) {

        Member member = findMemberBy(request.getUsername());

        validateSendMailRequest(request.getUniversityEmail(), member);

        EmailVerification emailVerification = request.toEntity(member, now());

        emailVerificationRepository.save(emailVerification);

        Mail mail = generateMail(emailVerification);

        Request sendRequest = generateSendRequest(mail);

        Response response = sendMail(sendRequest);

        checkSendStatus(response);
        
        setVerificationStatusAsPendingByUniversityEmail(member,
                emailVerification.getUniversityEmail());
    }

    private Mail generateMail(EmailVerification emailVerification) {
        Email from = new Email(MESSAGE_FROM);
        String subject = "인플리 학생 인증 메일입니다";
        Email to = new Email(emailVerification.getUniversityEmail());
        Content content = new Content(TEXT_PLAIN_VALUE,
                "안녕하세요. INFLI 입니다. 다음 링크를 클릭하시면 인증이 완료됩니다.\n" +
                        "https://infli.co/verification/student/email/"
                        + emailVerification.getCode() +
                        "\n" + "30분안에 인증하셔야 합니다.");

        return new Mail(from, subject, to, content);
    }

  
    @SneakyThrows({IOException.class})
    private Request generateSendRequest(Mail mail) {
        Request sendRequest = new Request();
        sendRequest.setMethod(POST);
        sendRequest.setEndpoint("mail/send");
        sendRequest.setBody(mail.build());
        return sendRequest;
    }

    @SneakyThrows({IOException.class})
    private Response sendMail(Request sendRequest) {
        return sendGrid.api(sendRequest);
    }
    
    private void checkSendStatus(Response response) {
        if (response.getStatusCode() != SC_ACCEPTED) {
            throw new InternalServerErrorException(EMAIL_SEND_FAILED);
        }
    }
    
    private void validateSendMailRequest(String universityEmail, Member member) {

        checkIfAllowedEmailSuffix(universityEmail);

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(EMAIL_VERIFICATION_ALREADY_EXISTS);
        }

        if (memberRepository.existsByVerifiedUniversityEmail(universityEmail)) {
            throw new ConflictException(DUPLICATED_UNIVERSITY_EMAIL);
        }
    }

    //TODO 람다로 변경 필요
    private void checkIfAllowedEmailSuffix(String universityEmail) {

        for (String suffix : ALLOWED_EMAIL_SUFFIX) {
            if (universityEmail.endsWith(suffix)) {
                return;
            }
        }
        throw new BadRequestException(INVALID_UNIVERSITY_EMAIL);
    }


    @Transactional
    public void verifyStudentMemberEmail(String secret) {

        EmailVerification emailVerification = findWithMemberBy(secret);

        Member member = emailVerification.getMember();

        validateVerifyStudentMemberEmailRequest(member, emailVerification);

        setVerificationStatusAsSuccess(member);
    }

    private EmailVerification findWithMemberBy(String secret) {
        return emailVerificationRepository.findWithMemberBy(secret)
                .orElseThrow(() -> new BadRequestException(INVALID_EMAIL_CODE));
    }

    private void validateVerifyStudentMemberEmailRequest(
            Member member, EmailVerification emailVerification) {

        if (member.getRole() != STUDENT || member.getVerificationStatus() != PENDING) {
            setVerificationStatusAsFail(member);
            throw new BadRequestException(INVALID_STUDENT_VERIFICATION_REQUEST);
        }

        LocalDateTime codeGeneratedTime = emailVerification.getCodeGeneratedTime();

        if (codeGeneratedTime.plusMinutes(30).isBefore(now())) {
            throw new BadRequestException(EMAIL_VERIFICATION_CODE_EXPIRED);
        }
    }
}
