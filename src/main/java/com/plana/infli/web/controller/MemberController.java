package com.plana.infli.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plana.infli.security.jwt.JwtProperties;
import com.plana.infli.service.AuthService;
import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.MemberCreateRequest;
import com.plana.infli.web.dto.response.member.MemberCreateResponse;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Validated
@RestController
public class MemberController {

	private final MemberService memberService;
	private final AuthService authService;
	private final JwtProperties jwtProperties;

	@PostMapping("/auth/signup")
	public ResponseEntity<MemberCreateResponse> signup(@RequestBody @Validated MemberCreateRequest request) {
		return ResponseEntity.ok(memberService.signup(request));
	}

	@GetMapping("/auth/validate/email")
	public ResponseEntity<Void> validateEmail(@RequestParam @Email String email) {
		memberService.checkEmailDuplicated(email);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/auth/validate/nickname")
	public ResponseEntity<Void> validateNickname(@RequestParam String nickname) {
		memberService.checkNicknameDuplicated(nickname);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/auth/reissue")
	public ResponseEntity<Void> reissueRefreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
		return ResponseEntity.ok().headers(authService.reissue(refreshToken)).build();
	}
}
