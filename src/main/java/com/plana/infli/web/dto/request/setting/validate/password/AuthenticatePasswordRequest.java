package com.plana.infli.web.dto.request.setting.validate.password;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthenticatePasswordRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @Builder
    private AuthenticatePasswordRequest(String password) {
        this.password = password;
    }

    public AuthenticatePasswordServiceRequest toServiceRequest(String email) {
        return AuthenticatePasswordServiceRequest.builder()
                .email(email)
                .password(password)
                .build();
    }
}
