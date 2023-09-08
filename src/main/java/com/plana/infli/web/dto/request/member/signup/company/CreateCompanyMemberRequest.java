package com.plana.infli.web.dto.request.member.signup.company;


import static com.plana.infli.service.MemberService.NICKNAME_REGEX;
import static com.plana.infli.service.MemberService.PASSWORD_REGEX;
import static com.plana.infli.service.MemberService.USERNAME_REGEX;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreateCompanyMemberRequest {

    @Pattern(regexp = USERNAME_REGEX, message = "영어, 숫자, 특수문자 -, _ 를 포함해서 5~20자리 이내로 입력해주세요")
    @NotNull(message = "아이디를 입력해주세요")
    private String username;

    @Pattern(regexp = NICKNAME_REGEX, message = "한글, 영어, 숫자를 포함해서 2~8자리 이내로 입력해주세요")
    @NotNull(message = "닉네임을 입력해주세요")
    private String nickname;

    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영어, 숫자, 특수문자를 포함해서 8~20자리 이내로 입력해주세요.")
    @NotNull(message = "비밀번호를 입력해주세요")
    private String password;

    @NotEmpty(message = "비밀번호 확인을 입력해주세요")
    private String passwordConfirm;

    @NotNull(message = "대학교 번호는 필수입니다.")
    private Long universityId;

    @NotEmpty(message = "회사 이름을 입력해주세요")
    private String companyName;

    @Builder
    private CreateCompanyMemberRequest(String username, String nickname, String password,
            String passwordConfirm, Long universityId, String companyName) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.universityId = universityId;
        this.companyName = companyName;
    }



    public CreateCompanyMemberServiceRequest toServiceRequest() {
        return CreateCompanyMemberServiceRequest.builder()
                .username(username)
                .nickname(nickname)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .universityId(universityId)
                .companyName(companyName)
                .build();
    }

}
