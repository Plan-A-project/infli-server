package com.plana.infli.web.dto.response.comment.view;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BestCommentResponse {

    // 댓글 ID 번호
    private final Long commentId;

    // 글 ID 번호
    private final Long postId;

    // 익명게시판에 작성된 댓글인지 여부
    // True -> 익명글임. 댓글 작성자의 닉네임 대신 식별자 번호가 조회됨
    // False -> 익명글 아님. 식별자 번호 대신 댓글 작성자의 닉네임이 조회됨
    private final boolean isAnonymousBoard;

    // 댓글 작성자의 닉네임
    // 익명 댓글인 경우 null
    @Nullable
    private final String nickname;

    // 댓글 작성자 프로필 사진 URL
    // 익명 댓글인 경우 null
    @Nullable
    private final String profileImageUrl;

    // 댓글 식별자
    private final Integer identifier;

    private final LocalDateTime createdAt;

    private final String content;

    private final int likesCount;


    @QueryProjection
    public BestCommentResponse(Long commentId, Long postId, boolean isAnonymousBoard,
            String nickname,
            String profileImageUrl, Integer identifier, LocalDateTime createdAt, String content,
            int likesCount) {
        this.commentId = commentId;
        this.postId = postId;
        this.isAnonymousBoard = isAnonymousBoard;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.identifier = identifier;
        this.createdAt = createdAt;
        this.content = content;
        this.likesCount = likesCount;
    }

}
