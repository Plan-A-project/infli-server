package com.plana.infli.web.dto.request.setting.verify.password;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyPasswordRequest {

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;

    @Builder
    private VerifyPasswordRequest(String password) {
        this.password = password;
    }

    public VerifyPasswordServiceRequest toServiceRequest(String username) {
        return VerifyPasswordServiceRequest.builder()
                .username(username)
                .password(password)
                .build();
    }
}
