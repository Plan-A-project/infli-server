package com.plana.infli.web.dto.response.comment.create;

import com.plana.infli.domain.Comment;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
public class CreateCommentResponse {

    // 생성된 댓글의 ID 번호
    private final Long commentId;

    // 댓글 식별자 번호
    private final int identifierNumber;

    @Builder
    private CreateCommentResponse(Long commentId, int identifierNumber) {
        this.commentId = commentId;
        this.identifierNumber = identifierNumber;
    }

    public static CreateCommentResponse of(Comment comment) {

        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .identifierNumber(comment.getIdentifierNumber())
                .build();
    }
}
