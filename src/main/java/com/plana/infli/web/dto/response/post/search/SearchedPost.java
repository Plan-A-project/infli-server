package com.plana.infli.web.dto.response.post.search;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SearchedDefaultPost extends DefaultPost {

    private final String content;

    @QueryProjection
    public SearchedDefaultPost(Long postId, String title, int likeCount, int viewCount,
            LocalDateTime createdAt, String thumbnailURL, String content) {
        super(postId, title, likeCount, viewCount, createdAt, thumbnailURL);
        this.content = content;
    }
}
