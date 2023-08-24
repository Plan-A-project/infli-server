package com.plana.infli.web.controller;


import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.response.member.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.member.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    @GetMapping("/certificate/members/student")
    @Operation(summary = "학생 인증을 요청한 회원 목록 조회")
    public LoadStudentVerificationsResponse loadStudentVerificationRequestImages(
            @AuthenticatedPrincipal String username, @RequestParam int page) {
        return memberService.loadStudentVerificationRequestImages(username, page);
    }

    @GetMapping("/certificate/members/company")
    @Operation(summary = "기업 인증을 요청한 회원 목록 조회")
    public LoadCompanyVerificationsResponse loadCompanyVerificationRequestImages(
            @AuthenticatedPrincipal String username, @RequestParam int page) {
        return memberService.loadCompanyVerificationRequestImages(username, page);
    }

    @PostMapping("/certificate/members/{memberId}")
    @Operation(summary = "인증 요청 관리자가 승인")
    public void setVerificationStatusAsSuccess(@AuthenticatedPrincipal String username,
            @PathVariable Long memberId) {
        memberService.setMemberVerificationStatusAsSuccess(username, memberId);
    }
}
