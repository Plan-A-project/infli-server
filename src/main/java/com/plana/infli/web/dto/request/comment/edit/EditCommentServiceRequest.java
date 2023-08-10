package com.plana.infli.web.dto.request.comment.edit;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EditCommentServiceRequest {

    private final String username;

    private final Long postId;

    private final Long commentId;

    private final String content;

    @Builder
    private EditCommentServiceRequest(String username, Long postId,
            Long commentId, String content) {

        this.username = username;
        this.postId = postId;
        this.commentId = commentId;
        this.content = content;
    }
}
