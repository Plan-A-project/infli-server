package com.plana.infli.service;

import static com.plana.infli.domain.EmailAuthentication.*;
import static com.plana.infli.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static java.time.LocalDateTime.*;

import com.plana.infli.domain.EmailAuthentication;
import com.plana.infli.domain.Member;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.email_authentication.EmailAuthenticationRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.request.member.emailAuthentication.SendEmailAuthenticationServiceRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MailService {

    private final MailSender mailSender;

    private final MemberRepository memberRepository;

    private final EmailAuthenticationRepository emailAuthenticationRepository;

    @Transactional
    public void sendMemberAuthenticationEmail(String email) {


//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
////
//        EmailAuthentication emailAuthentication = EmailAuthentication.c(
//                member);
//
//        String subject = "INFLI 회원 인증 메일";
//        String secret = emailAuthentication.getSecret();
//        String text = "안녕하세요. INFLI 입니다. 다음 링크를 클릭하시면 인증이 완료됩니다.\n" +
//                "http://localhost:8080/member/email/auth/" + secret + "\n" +
//                "30분안에 인증하셔야 합니다.";
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(member.getEmail());
//        message.setSubject(subject);
//        message.setText(text);
//
//        mailSender.send(message);
//
//        emailAuthenticationRepository.save(emailAuthentication);
    }

    @Transactional
    public void authenticateMemberEmail(String secret) {


        //    EmailAuthentication emailAuthentication = emailAuthenticationRepository.findAvailableEmailAuthentication(
        //            secret)
        //        .orElseThrow(() -> new NotFoundException(AUTHENTICATION_NOT_FOUND));
        //
        //    Member member = emailAuthentication.getMember();
        //
        //    authenticate(member);
    }

    @Transactional
    public void sendStudentAuthenticationEmail(SendEmailAuthenticationServiceRequest request) {

        Member member = findMemberBy(request.getEmail());

//        check
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//
//        mailSender.send(SimpleMailMessage );

//
//        EmailAuthentication emailAuthentication = create(member, now());
//
//        MimeMessage message = mailSender.createMimeMessage();
//
////        MimeMessageHelper helper = new MimeMessageHelper(
//                message,
//        )
//        String subject = "INFLI 학생 회원 인증 메일";
//        String secret = emailAuthentication.getSecret();
//        String text = "안녕하세요. INFLI 입니다. 다음 링크를 클릭하시면 인증이 완료됩니다.\n" +
//                "http://localhost:8080/member/student/auth/" + secret + "\n" +
//                "30분안에 인증하셔야 합니다.";
//
//        message.setTo(member.getEmail());
//        message.setSubject(subject);
//        message.setText(text);

//        mailSender.send(message);
//
//        emailAuthenticationRepository.save(emailAuthentication);
    }

    private Member findMemberBy(String email) {

        return memberRepository.findActiveMemberBy(email)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public void authenticateStudent(String secret) {

        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findAvailableEmailAuthentication(
                        secret)
                .orElseThrow(
                        () -> new NotFoundException(NotFoundException.AUTHENTICATION_NOT_FOUND));

        Member member = emailAuthentication.getMember();

        member.authenticateStudent();
    }

    @Transactional
    public void authenticateCompany(String secret) {

//        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findAvailableEmailAuthentication(
//                        secret)
//                .orElseThrow(
//                        () -> new NotFoundException(NotFoundException.AUTHENTICATION_NOT_FOUND));
//
//        Member member = emailAuthentication.getMember();
//
//        String subject = "INFLI 기업 회원 인증";
//        String text = "안녕하세요. INFLI 입니다. 기업 회원 인증이 완료되었습니다.";
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(member.getEmail());
//        message.setSubject(subject);
//        message.setText(text);
//
//        mailSender.send(message);
//        member.authenticateCompany();
    }

    @Transactional
    public void sendCompanyAuthenticationEmail(String email, MultipartFile multipartFile)
            throws MessagingException, IOException {

//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException(NotFoundException.MEMBER_NOT_FOUND));
//
////        EmailAuthentication emailAuthentication = EmailAuthentication.createStudentAuthentication(
////                member, email);
//
//        MimeMessage message = mailSender.createMimeMessage();
//
//        String subject = "INFLI 기업 회원 인증 메일";
//        String secret = emailAuthentication.getSecret();
//        String text = "안녕하세요. INFLI 입니다. 다음 링크를 클릭하시면 인증이 완료됩니다.\n" +
//                "http://localhost:8080/member/company/auth/" + secret + "\n" +
//                "30일 안에 인증하셔야 합니다.";
//
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
//        helper.setTo("infliauthentication@gmail.com");
//        helper.setSubject(subject);
//        helper.setText(text);
//
//        // 파일 첨부
//        helper.addAttachment(multipartFile.getOriginalFilename(), multipartFile);
//
//        mailSender.send(message);
//
//        emailAuthenticationRepository.save(emailAuthentication);
    }

}
