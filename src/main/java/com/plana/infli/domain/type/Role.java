package com.plana.infli.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {

	STUDENT("학생"),

	COMPANY("기업"),

	STUDENT_COUNCIL("학생회"),

	ADMIN("관리자"),
	;

	private final String value;
}
