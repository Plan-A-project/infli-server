package com.plana.infli.web.dto.request.member;

import com.plana.infli.domain.Member;

import lombok.Getter;

@Getter
public class MemberJoinRequest {

	private String email;
	private String password;
	private String name;

	public Member toEntity() {
		System.out.println(email);
		System.out.println(name);
		System.out.println(password);
		return new Member(email, name, password);
	}
}
