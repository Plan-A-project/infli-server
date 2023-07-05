package com.plana.infli.web.dto.request.commentlike.cancel.service;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelCommentLikeServiceRequest {


    private Long postId;

    private Long commentId;

    @Builder
    private CancelCommentLikeServiceRequest(Long postId, Long commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }
}
