package com.plana.infli.web.dto.response.profile.image;

import com.plana.infli.domain.embeddable.MemberProfileImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChangeProfileImageResponse {

    private final String originalUrl;

    private final String thumbnailUrl;

    @Builder
    public ChangeProfileImageResponse(String originalUrl, String thumbnailUrl) {
        this.originalUrl = originalUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ChangeProfileImageResponse of(MemberProfileImage profileImage) {
        return ChangeProfileImageResponse.builder()
                .originalUrl(profileImage.getOriginalUrl())
                .thumbnailUrl(profileImage.getThumbnailUrl())
                .build();
    }
}
