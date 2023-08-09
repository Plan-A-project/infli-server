package com.plana.infli.web.controller;

import static com.plana.infli.web.dto.response.ApiResponse.*;
import static org.springframework.http.HttpStatus.*;

import com.plana.infli.service.AuthService;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import com.plana.infli.web.dto.response.ApiResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final MemberService memberService;

    private final AuthService authService;

    @PostMapping("/signup/student")
    @ResponseStatus(CREATED)
    public ApiResponse<Long> signupAsStudentMember(@RequestBody @Validated CreateStudentMemberRequest request) {

        return created(memberService.signupAsStudentMember(request.toServiceRequest()));
    }

    @PostMapping("/signup/company")
    @ResponseStatus(CREATED)
    public ApiResponse<Long> signupAsCompanyMember(@RequestBody @Validated CreateCompanyMemberRequest request) {

        return created(memberService.signupAsCompanyMember(request.toServiceRequest()));
    }

    @GetMapping("/validate/email")
    public ApiResponse<String> validateEmail(@RequestParam @Email String email) {

        memberService.checkEmailDuplicate(email);
        return ok("사용 가능한 이메일 입니다");
    }

    @GetMapping("/validate/nickname")
    public ApiResponse<String> validateNickname(@RequestParam String nickname) {

        memberService.checkNicknameDuplicated(nickname);
        return ok("사용 가능한 닉네임 입니다");
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueRefreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok().headers(authService.reissue(refreshToken)).build();
    }
}
