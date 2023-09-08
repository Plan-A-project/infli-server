package com.plana.infli.web.dto.response.comment.view;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BestCommentResponse {

    // 댓글 ID 번호
    private final Long commentId;

    // 댓글 작성자의 닉네임
    // 익명 댓글인 경우 null
    @Nullable
    private final String nickname;

    // 댓글 작성자 프로필 사진 URL
    // 익명 댓글인 경우 null
    @Nullable
    private final String profileImageUrl;

    // 댓글 식별자
    private final int identifierNumber;

    private final LocalDateTime createdAt;

    private final String content;

    private final int likesCount;


    @QueryProjection
    public BestCommentResponse(Long commentId, @Nullable String nickname,
            @Nullable String profileImageUrl, int identifierNumber,
            LocalDateTime createdAt, String content, int likesCount) {

        this.commentId = commentId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.identifierNumber = identifierNumber;
        this.createdAt = createdAt;
        this.content = content;
        this.likesCount = likesCount;
    }

}
