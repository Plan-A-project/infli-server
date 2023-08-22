package com.plana.infli.web.dto.request.member.signup.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateStudentMemberRequest {

	@Pattern(regexp = "^[a-z0-9_-]{5,20}$", message = "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요")
	@NotNull(message = "아이디를 입력해주세요")
	private String username;

	@Pattern(regexp = "^[가-힣]{2,10}$", message = "이름은 한글로 2~10자리 이내로 입력해주세요")
	@NotNull(message = "이름을 입력해주세요")
	private String realName;

	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$",
			message = "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요.")
	@NotNull(message = "비밀번호를 입력해주세요")
	private String password;

	@NotBlank(message = "비밀번호 확인을 입력해주세요")
	private String passwordConfirm;

	@Pattern(regexp = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,8}$", message = "한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요")
	@NotNull(message = "닉네임을 입력해주세요")
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