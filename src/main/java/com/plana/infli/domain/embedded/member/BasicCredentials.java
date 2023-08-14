package com.plana.infli.domain.embedded.member;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class BasicCredentials {

    @Column(nullable = false, unique = true)
    private String nickname;

    private boolean isDeleted;

    private boolean isPolicyAccepted;

    @Builder
    private BasicCredentials(String nickname, boolean isDeleted, boolean isPolicyAccepted) {

        this.nickname = nickname;
        this.isDeleted = isDeleted;
        this.isPolicyAccepted = isPolicyAccepted;
    }

    private static BasicCredentials of(String nickname,
            boolean isDeleted, boolean isPolicyAccepted) {

        return BasicCredentials.builder()
                .nickname(nickname)
                .isDeleted(isDeleted)
                .isPolicyAccepted(isPolicyAccepted)
                .build();
    }


    public static BasicCredentials ofDefaultWithNickname(String nickname) {
        return of(nickname, false, false);
    }

    public static BasicCredentials ofNewNickname(BasicCredentials basicCredentials,
            String nickname) {

        return of(nickname, basicCredentials.isDeleted, basicCredentials.isPolicyAccepted);
    }

    public static BasicCredentials ofDeleted(BasicCredentials basicCredentials) {

        return BasicCredentials.builder()
                .isDeleted(true)
                .nickname(basicCredentials.nickname)
                .isPolicyAccepted(basicCredentials.isPolicyAccepted)
                .build();
    }

    public static BasicCredentials ofPolicyAccepted(BasicCredentials basicCredentials) {

        return BasicCredentials.builder()
                .isPolicyAccepted(true)
                .nickname(basicCredentials.nickname)
                .isDeleted(basicCredentials.isDeleted)
                .build();
    }
}
