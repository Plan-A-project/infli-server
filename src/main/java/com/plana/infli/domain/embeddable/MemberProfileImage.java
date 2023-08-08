package com.plana.infli.domain.embeddable;

import static lombok.AccessLevel.*;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
public class MemberProfileImage {

    private String originalUrl;

    private String thumbnailUrl;

    @Builder
    public MemberProfileImage(String originalUrl, String thumbnailUrl) {
        this.originalUrl = originalUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static MemberProfileImage defaultProfileImage() {
        return MemberProfileImage.builder()
                .originalUrl(null)
                .thumbnailUrl(null)
                .build();
    }

    public static MemberProfileImage of(String originalUrl, String thumbnailUrl) {
        return MemberProfileImage.builder()
                .originalUrl(originalUrl)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
