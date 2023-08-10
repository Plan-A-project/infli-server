package com.plana.infli.web.dto.request.commentlike.cancel;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelCommentLikeServiceRequest {

    private final String username;

    private final Long postId;

    private final Long commentId;

    @Builder
    private CancelCommentLikeServiceRequest(String username, Long postId, Long commentId) {
        this.username = username;
        this.postId = postId;
        this.commentId = commentId;
    }
}
