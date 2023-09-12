package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup/student")
    @ResponseStatus(CREATED)
    @Operation(summary = "학생 회원 가입")
    public Long signupAsStudentMember(@RequestBody @Validated CreateStudentMemberRequest request) {
        return memberService.signupAsStudentMember(request.toServiceRequest());
    }

    @PostMapping("/signup/company")
    @ResponseStatus(CREATED)
    @Operation(summary = "기업 회원 가입")
    public Long signupAsCompanyMember(@RequestBody @Validated CreateCompanyMemberRequest request) {
        return memberService.signupAsCompanyMember(request.toServiceRequest());
    }

    @GetMapping("/signup/username/{username}")
    @Operation(summary = "사용 가능한 아이디인지 확인")
    public String checkIsValidUsername(@PathVariable String username) {
        memberService.checkIsValidUsername(username);
        return "사용 가능한 아이디 입니다";
    }

    @GetMapping("/signup/nickname/{nickname}")
    @Operation(summary = "사용 가능한 닉네임인지 확인")
    public String checkIsValidNickname(@PathVariable String nickname) {
        memberService.checkIsValidNickname(nickname);
        return "사용 가능한 닉네임 입니다";
    }
}
