package com.plana.infli.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.plana.infli.service.MemberService;
import com.plana.infli.web.dto.request.member.MemberJoinRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/member/join")
	public void join(@RequestBody MemberJoinRequest memberJoinRequest) {
		memberService.join(memberJoinRequest);
	}

	@GetMapping("/hello")
	public String hello() {
		return "hello";
	}
}
