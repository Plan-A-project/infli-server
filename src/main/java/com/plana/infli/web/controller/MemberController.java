package com.plana.infli.web.controller;

import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.email.SendVerificationMailRequest;
import com.plana.infli.web.dto.response.member.verification.VerificationStatusResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/policy")
    @Operation(summary = "글 작성 이용 규칙 동의 여부 확인")
    public boolean checkMemberAcceptedWritePolicy(@AuthenticatedPrincipal String username) {
        return memberService.checkMemberAcceptedPolicy(username);
    }

    @PostMapping("/policy")
    @Operation(summary = "글 작성 이용 규칙 동의함")
    public void acceptWritePolicy(@AuthenticatedPrincipal String username) {
        memberService.acceptPolicy(username);
    }

    @GetMapping("/verification")
    @Operation(summary = "해당 회원의 인증 상태 조회")
    public VerificationStatusResponse loadVerificationStatus(@AuthenticatedPrincipal String username) {
        return memberService.loadVerificationStatus(username);
    }

    @PostMapping("/verification/student/email")
    public void sendVerificationEmail(@AuthenticatedPrincipal String username,
            @RequestBody @Validated SendVerificationMailRequest request) {
        memberService.sendVerificationMail(request.toServiceRequest(username));
    }

    @GetMapping("/verification/student/email/{code}")
    public void verifyStudentMemberEmail(@PathVariable String code) {
        memberService.verifyStudentMemberEmail(code);
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
