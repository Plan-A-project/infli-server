package com.plana.infli.web.controller;

import static com.plana.infli.web.dto.response.ApiResponse.ok;

import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.response.ApiResponse;
import com.plana.infli.web.dto.response.member.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.member.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminController {

    private final MemberService memberService;


    @GetMapping("/verification/student/certificate}")
    public ApiResponse<LoadStudentVerificationsResponse> loadStudentVerificationRequestImages(
            @AuthenticatedPrincipal String username, @RequestParam int page) {
        return ok(memberService.loadStudentVerificationRequestImages(username, page));
    }

    @PostMapping("/verification/certificate/certificate/{memberId}")
    public ApiResponse<Void> setStudentVerificationStatusAsSuccess(@AuthenticatedPrincipal String username,
            @PathVariable Long memberId) {

        memberService.setMemberVerificationStatusAsSuccess(username, memberId);
        return ok();
    }

    @GetMapping("/verification/company/certificate}")
    public ApiResponse<LoadCompanyVerificationsResponse> loadCompanyVerificationRequestImages(
            @AuthenticatedPrincipal String username, @RequestParam int page) {
        return ok(memberService.loadCompanyVerificationRequestImages(username, page));
    }

    @PostMapping("/verification/company/certificate/{memberId}")
    public ApiResponse<Void> setCompanyVerificationStatusAsSuccess(@AuthenticatedPrincipal String username,
            @PathVariable Long memberId) {

        memberService.setMemberVerificationStatusAsSuccess(username, memberId);
        return ok();
    }
}
