package com.plana.infli.domain.embeddable;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class MemberStatus {

    private boolean isDeleted;

    private boolean isAuthenticated;

    private boolean policyAccepted;

    @Builder
    private MemberStatus(boolean isDeleted,
            boolean isAuthenticated, boolean policyAccepted) {
        this.isDeleted = isDeleted;
        this.isAuthenticated = isAuthenticated;
        this.policyAccepted = policyAccepted;
    }

    public static MemberStatus defaultMemberStatus() {
        return create(false, false, false);
    }

    public static MemberStatus create(boolean isDeleted,
            boolean isAuthenticated, boolean hasAcceptedPolicy) {
        return builder()
                .isDeleted(isDeleted)
                .isAuthenticated(isAuthenticated)
                .policyAccepted(hasAcceptedPolicy)
                .build();
    }
}
