package com.plana.infli.web.dto.request.commentlike.create;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCommentLikeServiceRequest {

    private final String username;

    private final Long postId;

    private final Long commentId;

    @Builder
    public CreateCommentLikeServiceRequest(String username, Long postId, Long commentId) {
        this.username = username;
        this.postId = postId;
        this.commentId = commentId;
    }
}
