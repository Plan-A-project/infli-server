package com.plana.infli.web.dto.response.admin.member;

import com.plana.infli.domain.type.VerificationStatus;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignedUpStudentMember {

    private final Long memberId;

    private final Long universityId;

    private final String nickname;

    private final String realName;

    private final String universityEmail;

    private final String universityCertificateUrl;

    private final VerificationStatus status;

    private final String thumbnailUrl;

    private final boolean isPolicyAccepted;

    private final LocalDateTime joinedAt;

    @QueryProjection
    public SignedUpStudentMember(Long memberId, Long universityId, String nickname, String realName,
            String universityEmail, String universityCertificateUrl, VerificationStatus status,
            String thumbnailUrl, boolean isPolicyAccepted, LocalDateTime joinedAt) {
        this.memberId = memberId;
        this.universityId = universityId;
        this.nickname = nickname;
        this.realName = realName;
        this.universityEmail = universityEmail;
        this.universityCertificateUrl = universityCertificateUrl;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
        this.isPolicyAccepted = isPolicyAccepted;
        this.joinedAt = joinedAt;
    }
}
