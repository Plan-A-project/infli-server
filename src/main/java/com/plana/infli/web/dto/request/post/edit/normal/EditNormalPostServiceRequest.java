package com.plana.infli.web.dto.request.post.edit.normal;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditNormalPostServiceRequest {

    private final String email;

    private final Long postId;

    private final String title;

    private final String content;

    @Nullable
    private final String thumbnailUrl;

    @Builder
    public EditNormalPostServiceRequest(String email, Long postId,
            String title, String content, @Nullable String thumbnailUrl) {
        this.email = email;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
    }

}
