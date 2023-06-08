package com.plana.infli.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.plana.infli.domain.Member;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.request.member.MemberJoinRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public void join(MemberJoinRequest request) {
		memberRepository.save(new Member(
			request.getEmail(),
			passwordEncoder.encode(request.getPassword()),
			request.getPassword()));
	}

}
