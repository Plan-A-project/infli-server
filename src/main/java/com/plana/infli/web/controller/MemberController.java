package com.plana.infli.web.controller;

import com.plana.infli.service.MailService;
import com.plana.infli.web.dto.request.member.email.SendEmailAuthenticationRequest;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import jakarta.mail.MessagingException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class MemberController {

    private final MailService mailService;
    

    @PostMapping("/emails/verification")
    public void sendStudentAuthenticationEmail(@AuthenticatedPrincipal String username,
            @RequestBody @Validated SendEmailAuthenticationRequest request) {
        mailService.sendStudentAuthenticationEmail(request.toServiceRequest(username));
    }

    @PostMapping("/company/auth/send")
    public ResponseEntity<Void> sendCompanyAuthenticationEmail(@AuthenticatedPrincipal String email,
            @RequestBody MultipartFile file) throws MessagingException, IOException {
        mailService.sendCompanyAuthenticationEmail(email, file);
        return ResponseEntity.ok().build();
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
