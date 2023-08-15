package com.plana.infli.web.controller;

import com.plana.infli.service.MailService;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailRequest;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class MemberController {


    private final MemberService memberService;

    private final MailService mailService;
    

    @PostMapping("/verification/student/email")
    public void sendVerificationEmail(@AuthenticatedPrincipal String username,
            @RequestBody @Validated SendVerificationMailRequest request) {
        mailService.sendVerificationMail(request.toServiceRequest(username));
    }

    @GetMapping("/verification/student/email/{code}")
    public void verifyStudentMemberEmail(@PathVariable String code) {
        mailService.verifyStudentMemberEmail(code);
    }

    @PostMapping("/verification/student/certificate")
    public void uploadEnrollmentCertificateImage(@AuthenticatedPrincipal String username,
            @RequestParam MultipartFile file) {
        memberService.uploadUniversityCertificateImage(username, file);
    }

    @PostMapping("/verification/company/certificate")
    public void uploadCompanyCertificateImage(@AuthenticatedPrincipal String username,
            @RequestParam MultipartFile file) {

        memberService.uploadCompanyCertificateImage(username, file);
    }
}
