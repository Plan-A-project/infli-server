package com.plana.infli.web.dto.request.comment.edit.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class EditCommentServiceRequest {

    private final String email;

    private final Long postId;

    private final Long commentId;

    private final String content;

    @Builder
    private EditCommentServiceRequest(String email, Long postId,
            Long commentId, String content) {

        this.email = email;
        this.postId = postId;
        this.commentId = commentId;
        this.content = content;
    }
}
