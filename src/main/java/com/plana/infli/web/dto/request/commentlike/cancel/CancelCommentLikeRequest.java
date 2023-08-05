package com.plana.infli.web.dto.request.commentlike.cancel.controller;

import com.plana.infli.web.dto.request.commentlike.cancel.service.CancelCommentLikeServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelCommentLikeRequest {


    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long postId;

    @NotNull(message = "댓글 번호가 입력되지 않았습니다")
    private Long commentId;

    @Builder
    private CancelCommentLikeRequest(Long postId, Long commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }


    public CancelCommentLikeServiceRequest toServiceRequest() {
        return CancelCommentLikeServiceRequest.builder()
                .postId(postId)
                .commentId(commentId).build();
    }
}
