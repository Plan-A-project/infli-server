package com.plana.infli.web.dto.request.member.signup.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateStudentMemberRequest {

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	@NotBlank(message = "이름은 필수입니다.")
	private String name;

	@NotBlank(message = "비밀번호를 입력해주세요")
	private String password;

	@NotBlank(message = "비밀번호 확인을 입력해주세요")
	private String passwordConfirm;

	@NotBlank(message = "닉네임은 필수입니다.")
	private String nickname;

	@NotNull(message = "대학교 번호는 필수입니다.")
	private Long universityId;

	@Builder
	public CreateStudentMemberRequest(String email, String name, String password,
			String passwordConfirm, String nickname, Long universityId) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.nickname = nickname;
		this.universityId = universityId;
	}

	public CreateStudentMemberServiceRequest toServiceRequest() {
		return CreateStudentMemberServiceRequest.builder()
				.email(email)
				.name(name)
				.password(password)
				.passwordConfirm(passwordConfirm)
				.nickname(nickname)
				.universityId(universityId)
				.build();
	}
}