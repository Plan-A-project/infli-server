package com.plana.infli.domain.embedded.member;

import static lombok.AccessLevel.*;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
public class ProfileImage {

    private String originalUrl;

    private String thumbnailUrl;

    @Builder
    private ProfileImage(String originalUrl, String thumbnailUrl) {
        this.originalUrl = originalUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ProfileImage ofDefaultProfileImage() {
        return ProfileImage.builder()
                .originalUrl(null)
                .thumbnailUrl(null)
                .build();
    }

    public static ProfileImage of(String originalUrl, String thumbnailUrl) {
        return ProfileImage.builder()
                .originalUrl(originalUrl)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
