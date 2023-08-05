package com.plana.infli.web.dto.request.comment.create;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CreateCommentServiceRequest {

    private final String email;

    private final Long postId;

    private final Long parentCommentId;

    private final String content;

    @Builder
    private CreateCommentServiceRequest(String email, Long postId,
            Long parentCommentId, String content) {
        this.email = email;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }
}
