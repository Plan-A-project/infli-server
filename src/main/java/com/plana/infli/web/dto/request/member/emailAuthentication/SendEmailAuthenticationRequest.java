package com.plana.infli.web.dto.request.member.emailAuthentication;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendEmailAuthenticationRequest {

    @Email(message = "이메일 형식으로 입력해주세요")
    private String universityEmail;

    @Builder
    public SendEmailAuthenticationRequest(String universityEmail) {
        this.universityEmail = universityEmail;
    }

    public SendEmailAuthenticationServiceRequest toServiceRequest(String email) {
        return SendEmailAuthenticationServiceRequest.builder()
                .email(email)
                .universityEmail(universityEmail)
                .build();
    }
}
