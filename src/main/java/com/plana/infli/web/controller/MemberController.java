package com.plana.infli.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plana.infli.service.MailService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class MemberController {

	private final MailService mailService;

	@PostMapping("/email/auth/send")
	public ResponseEntity<Void> sendAuthenticationEmail(@AuthenticatedPrincipal String email) {
		mailService.sendMemberAuthenticationEmail(email);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/email/auth/{secret}")
	public ResponseEntity<Void> authenticateMemberEmail(@PathVariable String secret) {
		mailService.authenticateMemberEmail(secret);
		return ResponseEntity.ok().build();
	}
}
