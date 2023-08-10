package com.plana.infli.web.dto.response.post;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public abstract class DefaultPost {

    private final Long postId;

    private final String title;

    private int commentCount;

    private final boolean pressedLike;

    private final int likeCount;

    private final int viewCount;

    private final LocalDateTime createdAt;

    private final String thumbnailUrl;

    public DefaultPost(Long postId, String title, boolean pressedLike, int likeCount,
            int viewCount, LocalDateTime createdAt, String thumbnailUrl) {

        this.postId = postId;
        this.title = title;
        this.pressedLike = pressedLike;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void loadCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
