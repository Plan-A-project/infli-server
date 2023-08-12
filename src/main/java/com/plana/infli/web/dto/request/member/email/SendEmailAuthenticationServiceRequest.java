package com.plana.infli.web.dto.request.member.email;

import com.plana.infli.domain.EmailAuthentication;
import com.plana.infli.domain.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SendEmailAuthenticationServiceRequest {

    private final String username;

    private final String universityEmail;

    @Builder
    public SendEmailAuthenticationServiceRequest(String username, String universityEmail) {
        this.username = username;
        this.universityEmail = universityEmail;
    }

    public EmailAuthentication toEntity(Member member, LocalDateTime localDateTime) {
        return EmailAuthentication.create(member, localDateTime, universityEmail);
    }
}
