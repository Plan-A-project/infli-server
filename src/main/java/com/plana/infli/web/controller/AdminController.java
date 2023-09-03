package com.plana.infli.web.controller;


import com.plana.infli.service.AdminService;
import com.plana.infli.web.dto.response.admin.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.admin.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@RolesAllowed({"ADMIN"})
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/certificate/members/student")
    @Operation(summary = "학생 인증을 요청한 회원 목록 조회")
    public LoadStudentVerificationsResponse loadStudentVerificationRequestImages(
            @AuthenticatedPrincipal String username) {
        return adminService.loadStudentVerificationRequestImages(username);
    }

    @GetMapping("/certificate/members/company")
    @Operation(summary = "기업 인증을 요청한 회원 목록 조회")
    public LoadCompanyVerificationsResponse loadCompanyVerificationRequestImages(
            @AuthenticatedPrincipal String username) {
        return adminService.loadCompanyVerificationRequestImages(username);
    }

    @PostMapping("/certificate/members/{memberId}")
    @Operation(summary = "인증 요청 관리자가 승인")
    public void setVerificationStatusAsSuccess(@AuthenticatedPrincipal String username,
            @PathVariable Long memberId) {
        adminService.setStatusAsVerifiedMember(username, memberId);
    }
}
