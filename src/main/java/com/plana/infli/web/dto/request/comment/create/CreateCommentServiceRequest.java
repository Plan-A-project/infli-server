package com.plana.infli.web.dto.request.comment.create;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCommentServiceRequest {

    private final String username;

    private final Long postId;

    private final Long parentCommentId;

    private final String content;

    @Builder
    private CreateCommentServiceRequest(String username,
            Long postId, Long parentCommentId, String content) {

        this.username = username;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }
}
