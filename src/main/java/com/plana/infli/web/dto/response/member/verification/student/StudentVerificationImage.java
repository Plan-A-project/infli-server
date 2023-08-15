package com.plana.infli.web.dto.response.member.verification.student;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudentVerificationImage {

    private final Long memberId;

    private final String imageUrl;

    private final String realName;

    private final String universityName;

    private final LocalDateTime joinedAt;


    @QueryProjection
    public StudentVerificationImage(Long memberId, String imageUrl,
            String realName, String universityName, LocalDateTime joinedAt) {

        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.realName = realName;
        this.universityName = universityName;
        this.joinedAt = joinedAt;
    }
}
