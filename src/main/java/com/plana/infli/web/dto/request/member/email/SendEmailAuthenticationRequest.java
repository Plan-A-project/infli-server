package com.plana.infli.web.dto.request.member.email;

import jakarta.validation.constraints.Email;
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

    public SendEmailAuthenticationServiceRequest toServiceRequest(String username) {
        return SendEmailAuthenticationServiceRequest.builder()
                .username(username)
                .universityEmail(universityEmail)
                .build();
    }

}
