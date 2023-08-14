package com.plana.infli.web.dto.request.member.signup.company;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreateCompanyMemberRequest {

    @NotEmpty(message = "아이디를 입력해주세요")
    private String username;

    @NotEmpty(message = "별칭을 입력해주세요")
    private String nickname;

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;

    @NotEmpty(message = "비밀번호 확인을 입력해주세요")
    private String passwordConfirm;

    @NotNull(message = "대학교 Id를 입력해주세요")
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
