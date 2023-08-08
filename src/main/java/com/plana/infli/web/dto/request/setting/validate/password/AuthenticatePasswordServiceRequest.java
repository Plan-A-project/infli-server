package com.plana.infli.web.dto.request.setting.validate.password;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthenticatePasswordServiceRequest {

    private final String email;

    private final String password;

    @Builder
    public AuthenticatePasswordServiceRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
