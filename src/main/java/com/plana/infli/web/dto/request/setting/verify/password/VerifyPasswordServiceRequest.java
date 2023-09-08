package com.plana.infli.web.dto.request.setting.verify.password;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VerifyPasswordServiceRequest {

    private final String username;

    private final String password;

    @Builder
    private VerifyPasswordServiceRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
