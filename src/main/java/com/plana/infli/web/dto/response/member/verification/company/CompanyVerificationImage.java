package com.plana.infli.web.dto.response.member.verification.company;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompanyVerificationImage {

    private final Long memberId;

    private final String imageUrl;

    private final String companyName;

    private final LocalDateTime joinedAt;

    @QueryProjection
    public CompanyVerificationImage(Long memberId, String imageUrl, String companyName,
            LocalDateTime joinedAt) {
        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.companyName = companyName;
        this.joinedAt = joinedAt;
    }
}
