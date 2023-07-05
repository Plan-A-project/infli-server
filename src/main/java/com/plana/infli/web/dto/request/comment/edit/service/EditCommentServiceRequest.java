package com.plana.infli.web.dto.request.comment.edit.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditCommentServiceRequest {

    private Long postId;

    private Long commentId;

    private String content;

    @Builder
    private EditCommentServiceRequest(Long postId, Long commentId, String content) {
        this.postId = postId;
        this.commentId = commentId;
        this.content = content;
    }
}
