package com.plana.infli.web.dto.request.member.signup.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateStudentMemberRequest {

	@Email(message = "아이디를 입력해주세요")
	private String username;

	@NotBlank(message = "이름을 입력해주세요")
	private String realName;

	@NotBlank(message = "비밀번호를 입력해주세요")
	private String password;

	@NotBlank(message = "비밀번호 확인을 입력해주세요")
	private String passwordConfirm;

	@NotBlank(message = "닉네임을 입력해주세요")
	private String nickname;

	@NotNull(message = "대학교 번호는 필수입니다.")
	private Long universityId;

	@Builder
	public CreateStudentMemberRequest(String username, String realName, String password,
			String passwordConfirm, String nickname, Long universityId) {
		this.username = username;
		this.realName = realName;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.nickname = nickname;
		this.universityId = universityId;
	}

	public CreateStudentMemberServiceRequest toServiceRequest() {
		return CreateStudentMemberServiceRequest.builder()
				.username(username)
				.realName(realName)
				.password(password)
				.passwordConfirm(passwordConfirm)
				.nickname(nickname)
				.universityId(universityId)
				.build();
	}

}