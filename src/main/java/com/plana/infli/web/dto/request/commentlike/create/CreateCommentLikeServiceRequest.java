package com.plana.infli.web.dto.request.commentlike.create;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCommentLikeServiceRequest {

    private final String email;

    private final Long postId;

    private final Long commentId;

    @Builder
    public CreateCommentLikeServiceRequest(String email, Long postId, Long commentId) {
        this.email = email;
        this.postId = postId;
        this.commentId = commentId;
    }
}
