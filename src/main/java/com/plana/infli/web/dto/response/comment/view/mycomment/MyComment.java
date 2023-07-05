package com.plana.infli.web.dto.response.comment.view.mycomment;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MyComment {

    private final Long commentId;

    private final Long postId;

    private final String content;

    private final LocalDateTime createdAt;


    @QueryProjection
    public MyComment(Long commentId, Long postId, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
