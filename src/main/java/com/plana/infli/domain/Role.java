package com.plana.infli.domain;

import static com.plana.infli.domain.BoardType.*;
import static java.util.Collections.*;
import static java.util.List.*;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {

	UNCERTIFIED("ROLE_UNCERTIFIED", "미인증 회원"),
	STUDENT("ROLE_STUDENT", "학생 회원"),
	COMPANY("ROLE_COMPANY", "기업 회원"),
	STUDENT_COUNCIL("ROLE_STUDENT_COUNCIL", "학생회 회원"),
	ADMIN("ROLE_ADMIN", "관리자"),;

	private final String key;

	private final String value;

}
