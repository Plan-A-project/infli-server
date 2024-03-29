package com.plana.infli.web.dto.request.member.email;

import com.plana.infli.domain.EmailVerification;
import com.plana.infli.domain.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SendVerificationMailServiceRequest {

    private final String username;

    private final String universityEmail;

    @Builder
    public SendVerificationMailServiceRequest(String username, String universityEmail) {
        this.username = username;
        this.universityEmail = universityEmail;
    }

    public EmailVerification toEntity(Member member, LocalDateTime localDateTime) {
        return EmailVerification.create(member, localDateTime, universityEmail);
    }
}
