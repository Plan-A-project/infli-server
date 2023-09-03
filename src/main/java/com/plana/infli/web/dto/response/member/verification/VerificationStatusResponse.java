package com.plana.infli.web.dto.response.member.verification;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VerificationStatusResponse {

    private final VerificationStatus status;

    @Builder
    private VerificationStatusResponse(VerificationStatus status) {
        this.status = status;
    }

    public static VerificationStatusResponse of(Member member) {
        return VerificationStatusResponse.builder()
                .status(member.getVerificationStatus())
                .build();
    }
}
