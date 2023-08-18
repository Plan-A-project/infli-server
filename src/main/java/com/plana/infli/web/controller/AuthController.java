package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup/student")
    @ResponseStatus(CREATED)
    public Long signupAsStudentMember(@RequestBody @Validated CreateStudentMemberRequest request) {
        return memberService.signupAsStudentMember(request.toServiceRequest());
    }

    @PostMapping("/signup/company")
    @ResponseStatus(CREATED)
    public Long signupAsCompanyMember(@RequestBody @Validated CreateCompanyMemberRequest request) {
        return memberService.signupAsCompanyMember(request.toServiceRequest());
    }

    @GetMapping("/signup/duplication/username/{username}")
    public String validateUsername(@PathVariable String username) {
        memberService.checkUsernameDuplicate(username);
        return "사용 가능한 아이디 입니다";
    }

    @GetMapping("/signup/duplication/nickname/{nickname}")
    public String validateNickname(@PathVariable String nickname) {
        memberService.checkNicknameDuplicate(nickname);
        return "사용 가능한 닉네임 입니다";
    }
}
