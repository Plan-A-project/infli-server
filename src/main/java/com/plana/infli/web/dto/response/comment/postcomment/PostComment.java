package com.plana.infli.web.dto.response.comment.postcomment;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostComment {

    private final Long id;

    private final boolean isEnabled;

    private final String content;

    private final String nickname;

    private final boolean isMyComment;

    private final boolean isParentComment;

    private final LocalDateTime createdAt;

    @QueryProjection
    public PostComment(Long id, boolean isEnabled, String content, String nickname,
            boolean isMyComment, boolean isParentComment, LocalDateTime createdAt) {

        this.isEnabled = isEnabled;
        this.id = id;
        this.content = isEnabled ? content : "삭제된 댓글입니다";
        this.nickname = nickname;
        this.isMyComment = isMyComment;
        this.isParentComment = isParentComment;
        this.createdAt = createdAt;
    }
}
