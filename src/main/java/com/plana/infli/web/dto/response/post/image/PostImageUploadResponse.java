package com.plana.infli.web.dto.response.post.image;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostImageUploadResponse {

    private final String thumbnailImageUrl;

    private final List<String> originalImageUrls;

    @Builder
    public PostImageUploadResponse(String thumbnailImageUrl, List<String> originalImageUrls) {
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.originalImageUrls = originalImageUrls;
    }

    public static PostImageUploadResponse of(String thumbnailImageUrl,
            List<String> originalImageUrls) {
        return PostImageUploadResponse.builder()
                .thumbnailImageUrl(thumbnailImageUrl)
                .originalImageUrls(originalImageUrls)
                .build();
    }
}
