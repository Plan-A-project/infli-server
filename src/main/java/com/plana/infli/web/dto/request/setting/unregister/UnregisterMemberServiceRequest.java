package com.plana.infli.web.dto.request.setting.unregister;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UnregisterMemberServiceRequest {

    private final String authenticatedEmail;

    private final String email;

    private final String password;

    private final String name;

    @Builder
    public UnregisterMemberServiceRequest(String authenticatedEmail,
            String email, String password, String name) {
        this.authenticatedEmail = authenticatedEmail;
        this.email = email;
        this.password = password;
        this.name = name;
    }

}
