package com.plana.infli.web.dto.response.member;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateResponse {

	private String email;
	private String name;
	private String nickname;

	private Role role;

	private MemberCreateResponse(String email, String name, String nickname, Role role) {
		this.email = email;
		this.name = name;
		this.nickname = nickname;
	}

	public static MemberCreateResponse from(Member member) {
		return new MemberCreateResponse(member.getEmail(), member.getName(), member.getNickname(), member.getRole());
	}
}
