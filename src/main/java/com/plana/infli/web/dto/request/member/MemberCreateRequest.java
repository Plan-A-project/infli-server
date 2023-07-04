package com.plana.infli.web.dto.request.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCreateRequest {

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;
	@NotBlank(message = "이름은 필수입니다.")
	private String name;
	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
	@NotBlank(message = "닉네임은 필수입니다.")
	private String nickname;

	@NotNull(message = "대학교 번호는 필수입니다.")
	private Long universityId;

}
