package com.plana.infli.web.dto.response.post.search;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CommentCount {

    private final Long postId;

    @QueryProjection
    public CommentCount(Long postId) {
        this.postId = postId;
    }
}
