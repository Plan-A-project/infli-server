package com.plana.infli.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {

	UNCERTIFIED_STUDENT("미인증 학생 회원"),

	UNCERTIFIED_COMPANY("미인증 기업 회원"),

	STUDENT("학생 회원"),

	COMPANY("기업 회원"),

	STUDENT_COUNCIL("학생회 회원"),

	ADMIN("관리자"),
	;

	private final String value;

}
