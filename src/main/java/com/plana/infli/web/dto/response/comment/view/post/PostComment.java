package com.plana.infli.web.dto.response.comment.view.post;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostComment {

    // 댓글 ID 번호
    private final Long id;

    // 댓글 작성자 닉네임
    // 익명 댓글인 경우 null
    @Nullable
    private final String nickname;

    // 회원 프로필 사진 URL
    // 익명 댓글인 경우 null
    @Nullable
    private final String profileImageUrl;


    // 해당 댓글이 삭제되었는지 여부
    // True -> 삭제됨
    // False -> 삭제되지 않았음
    private final boolean isDeleted;

    // 댓글 식별자
    // 글 작성자가 자신의 글에 댓글을 작성한 경우 : "작성자"로 표시됨
    // 그 외의 경우 : 익명1, 익명2, 익명3 ... 로 표시됨
    //TODO 위의 주석 수정 필요
    private final Integer identifier;

    // 댓글 생성 시간
    private final LocalDateTime createdAt;

    // 댓글 내용
    private final String content;

    // 해당 댓글이 내가 작성한 댓글인지 여부
    // True -> 내가 작성한 댓글임
    // False -> 내가 작성한 댓글 아님
    private final boolean isMyComment;

    // 해당 댓글에 눌린 좋아요 갯수
    private final int likesCount;

    // 내가 해당 댓글에 좋아요를 눌렀는지 여부
    // True -> 내가 좋아요 누른 댓글
    // False -> 내가 좋아요 누르지 않은 댓글
    private final boolean pressedLikeOnThisComment;

    // 해당 댓글이 일반 댓글인지, 대댓글인지 여부
    // True -> 일반댓글임
    // False -> 대댓글임
    private final boolean isParentComment;

    //해당 댓글이 수정되었는지 여부
    // True ->  수정됨
    // False -> 수정되지 않은 댓글임
    private final boolean isEdited;


    // 글 작성자가 본인의 글에 작성한 댓글인지 여부
    // True -> 글 작성자가 작성한 댓글임
    // False -> 글 작성자가 작성한 댓글 아님
    private final boolean isPostWriter;


    @QueryProjection
    public PostComment(Long id, String nickname, String profileImageUrl, boolean isDeleted,
            Integer identifier, LocalDateTime createdAt, String content, boolean isMyComment,
            int likesCount, boolean pressedLikeOnThisComment, boolean isParentComment,
            boolean isEdited, boolean isPostWriter) {

        this.id = id;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.isDeleted = isDeleted;
        this.identifier = identifier;
        this.createdAt = createdAt;
        this.content = isDeleted ? "삭제된 댓글입니다" : content;
        this.isMyComment = isMyComment;
        this.likesCount = likesCount;
        this.pressedLikeOnThisComment = pressedLikeOnThisComment;
        this.isParentComment = isParentComment;
        this.isEdited = isEdited;
        this.isPostWriter = isPostWriter;
    }
}
