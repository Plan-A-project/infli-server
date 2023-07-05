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

    // 댓글 내용
    private final String content;

    // 댓글 작성자 ID
    private final Long writerId;

    // 댓글이 작성된 글 ID
    private final Long postId;

    // 댓글 식별자 번호
    private final Integer identifierNumber;

    // 댓글인지 대댓글인지 여부
    // True -> 댓글임
    // False -> 대댓글임
    private final boolean isParentComment;

    @Builder
    private CreateCommentResponse(Long commentId, String content, Long writerId,
            Integer identifierNumber, Long postId, boolean isParentComment) {

        this.commentId = commentId;
        this.content = content;
        this.writerId = writerId;
        this.postId = postId;
        this.identifierNumber = identifierNumber;
        this.isParentComment = isParentComment;
    }

    public static CreateCommentResponse of(Comment comment, Post post,
            Member member) {

        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writerId(member.getId())
                .postId(post.getId())
                .identifierNumber(comment.getIdentifierNumber())
                .isParentComment(comment.isParentComment())
                .build();
    }
}
