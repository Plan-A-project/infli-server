package com.plana.infli.domain.embedded.member;

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
    private MemberStatus(boolean isDeleted, boolean policyAccepted) {
        this.isDeleted = isDeleted;
        this.policyAccepted = policyAccepted;
    }

    public static MemberStatus defaultStatus() {
        return create(false, false);
    }

    public static MemberStatus create(boolean isDeleted, boolean hasAcceptedPolicy) {
        return builder()
                .isDeleted(isDeleted)
                .policyAccepted(hasAcceptedPolicy)
                .build();
    }

    public static MemberStatus ofDeleted(boolean policyAccepted) {
        return MemberStatus.builder()
                .isDeleted(true)
                .policyAccepted(policyAccepted)
                .build();
    }

    public static MemberStatus ofPolicyAccepted() {
        return MemberStatus.builder()
                .isDeleted(false)
                .policyAccepted(true)
                .build();
    }
}
