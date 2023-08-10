package com.plana.infli.web.dto.request.member.emailAuthentication;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SendEmailAuthenticationServiceRequest {

    private final String email;

    private final String universityEmail;

    @Builder
    public SendEmailAuthenticationServiceRequest(String email, String universityEmail) {
        this.email = email;
        this.universityEmail = universityEmail;
    }
}
