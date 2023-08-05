package com.plana.infli.web.dto.request.commentlike.cancel;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelCommentLikeServiceRequest {

    private final String email;

    private final Long postId;

    private final Long commentId;

    @Builder
    private CancelCommentLikeServiceRequest(String email, Long postId, Long commentId) {
        this.email = email;
        this.postId = postId;
        this.commentId = commentId;
    }
}
