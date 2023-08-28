package com.plana.infli.web.dto.request.member.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendVerificationMailRequest {

    @Email(message = "이메일 형식으로 입력해주세요")
    @NotEmpty(message = "대학교 이메일을 입력해주세요")
    private String universityEmail;

    @Builder
    public SendVerificationMailRequest(String universityEmail) {
        this.universityEmail = universityEmail;
    }

    public SendVerificationMailServiceRequest toServiceRequest(String username) {
        return SendVerificationMailServiceRequest.builder()
                .username(username)
                .universityEmail(universityEmail)
                .build();
    }
}
