package com.plana.infli.web.dto.request.comment.create.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCommentServiceRequest {

    private Long postId;

    private Long parentCommentId;

    private String content;


    @Builder
    private CreateCommentServiceRequest(Long postId, Long parentCommentId, String content) {
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }
}
