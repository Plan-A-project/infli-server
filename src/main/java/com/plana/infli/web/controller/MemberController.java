package com.plana.infli.web.controller;

import com.plana.infli.service.MailService;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailRequest;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    

    @PostMapping("/emails/verification")
    public void sendVerificationEmail(@AuthenticatedPrincipal String username,
            @RequestBody @Validated SendVerificationMailRequest request) {
        mailService.sendVerificationMail(request.toServiceRequest(username));
    }

    @PostMapping("/company/auth/send")
    public void receiveCompanyCertificateImage(@AuthenticatedPrincipal String username,
            @RequestParam MultipartFile file) {

        memberService.receiveCompanyCertificateImage(username, file);
    }

    @GetMapping("/email/auth/{secret}")
    public ResponseEntity<Void> authenticateMemberEmail(@PathVariable String secret) {
        mailService.authenticateMemberEmail(secret);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/company/auth/{secret}")
    public ResponseEntity<Void> authenticateCompany(@PathVariable String secret) {
        mailService.authenticateCompany(secret);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/student/auth/{secret}")
    public ResponseEntity<Void> authenticateStudent(@PathVariable String secret) {
        mailService.authenticateStudent(secret);
        return ResponseEntity.ok().build();
    }
}
