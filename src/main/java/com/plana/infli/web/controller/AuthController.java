package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AuthController {

    public static final String USERNAME_REGEX = "^[a-z0-9_-]{5,20}$";

    public static final String REAL_NAME_REGEX = "^[가-힣]{2,10}$";

    public static final String NICKNAME_REGEX = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,8}$";

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$";

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

    @GetMapping("/signup/username/{username}")
    public String checkIsValidUsername(@PathVariable String username) {
        memberService.checkIsValidUsername(username);
        return "사용 가능한 아이디 입니다";
    }

    @GetMapping("/signup/nickname/{nickname}")
    public String checkIsValidNickname(@PathVariable String nickname) {
        memberService.checkNicknameDuplicate(nickname);
        return "사용 가능한 닉네임 입니다";
    }
}
