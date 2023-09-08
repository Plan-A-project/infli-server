package com.plana.infli.web.dto.response.profile.image;

import com.plana.infli.domain.embedded.member.ProfileImage;
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

    public static ChangeProfileImageResponse of(ProfileImage profileImage) {
        return ChangeProfileImageResponse.builder()
                .originalUrl(profileImage.getOriginalUrl())
                .thumbnailUrl(profileImage.getThumbnailUrl())
                .build();
    }
}
