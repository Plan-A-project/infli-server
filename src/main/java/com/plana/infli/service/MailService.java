package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.EMAIL_VERIFICATION_ALREADY_EXISTS;
import static com.plana.infli.infra.exception.custom.BadRequestException.EMAIL_VERIFICATION_CODE_EXPIRED;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_EMAIL_CODE;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_STUDENT_VERIFICATION_REQUEST;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_UNIVERSITY_EMAIL;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_UNIVERSITY_EMAIL;
import static com.plana.infli.infra.exception.custom.InternalServerErrorException.EMAIL_SEND_FAILED;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.sendgrid.Method.*;
import static jakarta.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.lang.String.*;
import static java.time.LocalDateTime.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.plana.infli.domain.EmailVerification;
import com.plana.infli.domain.Member;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.InternalServerErrorException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.emailVerification.EmailVerificationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.service.aop.upload.Upload;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailServiceRequest;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class MailService {

    private final MemberRepository memberRepository;

    private final EmailVerificationRepository emailVerificationRepository;

    private final SendGrid sendGrid;

    private static final String MESSAGE_FROM = "no-reply@infli.co";

    private static final String ALLOWED_EMAIL_SUFFIX = "@fudan.edu.cn";

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

        sendMail(mail);

        setVerificationStatusAsPendingByUniversityEmail(member,
                emailVerification.getUniversityEmail());
    }

    private void validateSendMailRequest(String universityEmail, Member member) {
        if (universityEmail.endsWith(ALLOWED_EMAIL_SUFFIX) == false) {
            throw new BadRequestException(INVALID_UNIVERSITY_EMAIL);
        }

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(EMAIL_VERIFICATION_ALREADY_EXISTS);
        }

        if (memberRepository.existsByVerifiedUniversityEmail(universityEmail)) {
            throw new ConflictException(DUPLICATED_UNIVERSITY_EMAIL);
        }
    }

    @SneakyThrows({IOException.class})
    public void sendMail(Mail mail) {
        Request sendRequest = new Request();
        sendRequest.setMethod(POST);
        sendRequest.setEndpoint("mail/send");
        sendRequest.setBody(mail.build());
        Response response = sendGrid.api(sendRequest);
        checkSendStatus(response);
    }

    private void checkSendStatus(Response response) {
        if (response.getStatusCode() != SC_ACCEPTED) {
            throw new InternalServerErrorException(EMAIL_SEND_FAILED);
        }
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



    private Member findMemberBy(String email) {

        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

}
