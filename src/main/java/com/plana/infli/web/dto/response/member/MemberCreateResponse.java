package com.plana.infli.web.dto.response.member;

import com.plana.infli.domain.Member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateResponse {

	private String email;
	private String name;
	private String nickname;

	private MemberCreateResponse(String email, String name, String nickname) {
		this.email = email;
		this.name = name;
		this.nickname = nickname;
	}

	public static MemberCreateResponse from(Member member) {
		return new MemberCreateResponse(member.getEmail(), member.getName(), member.getNickname());
	}
}
