package com.plana.infli.web.dto.request.commentlike.create.controller;

import com.plana.infli.web.dto.request.commentlike.create.service.CreateCommentLikeServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCommentLikeRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long postId;

    @NotNull(message = "댓글 번호가 입력되지 않았습니다")
    private Long commentId;

    @Builder
    private CreateCommentLikeRequest(Long postId, Long commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }

    public CreateCommentLikeServiceRequest toServiceRequest() {
        return CreateCommentLikeServiceRequest.builder()
                .postId(postId)
                .commentId(commentId)
                .build();
    }
}
