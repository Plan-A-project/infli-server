package com.plana.infli.web.controller;

import com.plana.infli.service.AuthService;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.signup.CompanyMemberCreateRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RequestMapping("/auth")
@RestController
public class AuthController {

  private final MemberService memberService;
  private final AuthService authService;

  @PostMapping("/signup/student")
  public ResponseEntity<Void> signupStudent(@RequestBody @Validated CreateStudentMemberRequest request) {
    memberService.signupMember(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/signup/company")
  public ResponseEntity<Void> signupCompany(
      @RequestBody @Validated CompanyMemberCreateRequest request) {
    memberService.signupCompanyMember(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/validate/email")
  public ResponseEntity<Void> validateEmail(@RequestParam @Email String email) {
    memberService.checkEmailDuplicated(email);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/validate/nickname")
  public ResponseEntity<Void> validateNickname(@RequestParam String nickname) {
    memberService.checkNicknameDuplicated(nickname);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reissue")
  public ResponseEntity<Void> reissueRefreshToken(
      @RequestHeader("Refresh-Token") String refreshToken) {
    return ResponseEntity.ok().headers(authService.reissue(refreshToken)).build();
  }
}
