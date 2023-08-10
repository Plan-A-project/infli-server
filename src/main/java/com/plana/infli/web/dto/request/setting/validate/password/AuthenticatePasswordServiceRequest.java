package com.plana.infli.web.dto.request.setting.validate.password;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthenticatePasswordServiceRequest {

    private final String username;

    private final String password;

    @Builder
    public AuthenticatePasswordServiceRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
