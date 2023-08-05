package com.plana.infli.web.dto.request.commentlike.create.service;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCommentLikeServiceRequest {

    private Long postId;

    private Long commentId;

    @Builder
    public CreateCommentLikeServiceRequest(Long postId, Long commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }
}
