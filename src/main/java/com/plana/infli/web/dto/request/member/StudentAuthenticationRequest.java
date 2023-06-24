package com.plana.infli.web.dto.request.member;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentAuthenticationRequest {

	@Email
	private String studentEmail;
}
