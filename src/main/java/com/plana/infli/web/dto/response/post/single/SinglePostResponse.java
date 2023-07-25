package com.plana.infli.web.dto.response.post.single;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SinglePostResponse {

    // 게시판 이름
    private final String boardName;

    // 게시판 ID
    private final Long boardId;

    // 게시글 종류
    private final String postType;

    @Nullable
    // 글 작성자 닉네임
    // 익명글인 경우 null 반환
    private final String nickname;

    // 글 Id 번호
    private final Long postId;

    // 글 제목
    private final String title;

    // 글 내용
    private final String content;

    // 작성 시간
    private final LocalDateTime createdAt;

    // 내가 작성한 글인지 여부
    // True -> 지금 글 조회중인 회원이 글 작성자임

    private final boolean isMyPost;

    private final boolean isAdmin;

    private final int viewCount;

    private final int likeCount;

    private final String thumbnailURL;

    @QueryProjection
    public SinglePostResponse(String boardName, Long boardId, String postType, String nickname,
            Long postId, String title, String content, LocalDateTime createdAt, boolean isMyPost,
            boolean isAdmin, int viewCount, int likeCount, String thumbnailURL) {

        this.boardName = boardName;
        this.boardId = boardId;
        this.postType = postType;
        this.nickname = nickname;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isMyPost = isMyPost;
        this.isAdmin = isAdmin;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.thumbnailURL = thumbnailURL;
    }
}
