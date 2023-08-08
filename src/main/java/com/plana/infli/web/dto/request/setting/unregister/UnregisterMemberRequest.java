package com.plana.infli.web.dto.request.setting.unregister;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UnregisterMemberRequest {

    @NotEmpty(message = "이메일을 입력해주세요")
    private String email;

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;

    @NotEmpty(message = "회원 이름을 입력해주세요")
    private String name;

    @Builder
    private UnregisterMemberRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public UnregisterMemberServiceRequest toServiceRequest(String authenticatedEmail) {
        return UnregisterMemberServiceRequest.builder()
                .authenticatedEmail(authenticatedEmail)
                .email(email)
                .password(password)
                .name(name)
                .build();
    }
}
