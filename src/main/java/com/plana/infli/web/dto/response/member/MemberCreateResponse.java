package com.plana.infli.web.dto.response.member;

import com.plana.infli.domain.type.MemberRole;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateResponse {

	private String email;
	private String name;
	private String nickname;

	private MemberRole memberRole;

	private boolean isAuthenticated;

	private MemberCreateResponse(String email, String name, String nickname, MemberRole memberRole, boolean isAuthenticated) {
		this.email = email;
		this.name = name;
		this.nickname = nickname;
		this.memberRole = memberRole;
		this.isAuthenticated = isAuthenticated;
	}

//	public static MemberCreateResponse from(Member member) {
//		return new MemberCreateResponse(member.getEmail(), member.getName(), member.getNickname(), member.getRole(),
//			member.isAuthenticated());
//	}
}
