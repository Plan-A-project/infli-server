package com.plana.infli.web.dto.response.post.single;

import static com.plana.infli.web.dto.response.post.single.SinglePostResponse.RecruitmentInfoResponse.*;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SinglePostResponse extends DefaultPost {

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

    // 글 내용
    private final String content;

    // 내가 작성한 글인지 여부
    // True -> 지금 글 조회중인 회원이 글 작성자임
    private final boolean isMyPost;

    private final boolean isAdmin;

    private final String thumbnailURL;

    @Nullable
    private final RecruitmentInfoResponse recruitmentInfo;

    @QueryProjection
    public SinglePostResponse(String boardName, Long boardId, String postType, String nickname,
            Long postId, String title, String content, LocalDateTime createdAt, boolean isMyPost,
            boolean isAdmin, int viewCount, int likeCount, boolean pressedLike,String thumbnailURL, String companyName,
            LocalDateTime recruitmentStartedDate, LocalDateTime recruitmentEndDate) {

        super(postId, title, pressedLike, likeCount, viewCount, createdAt, thumbnailURL);

        this.boardName = boardName;
        this.boardId = boardId;
        this.postType = postType;
        this.nickname = nickname;
        this.content = content;
        this.isMyPost = isMyPost;
        this.isAdmin = isAdmin;
        this.thumbnailURL = thumbnailURL;
        this.recruitmentInfo = create(companyName, recruitmentStartedDate, recruitmentEndDate);
    }

    @Getter
    public static class RecruitmentInfoResponse {

        private final String companyName;

        private final LocalDateTime recruitmentStartedDate;

        private final LocalDateTime recruitmentEndDate;

        @Builder
        public RecruitmentInfoResponse(String companyName, LocalDateTime recruitmentStartedDate,
                LocalDateTime recruitmentEndDate) {
            this.companyName = companyName;
            this.recruitmentStartedDate = recruitmentStartedDate;
            this.recruitmentEndDate = recruitmentEndDate;
        }

        public static RecruitmentInfoResponse create(String companyName,
                LocalDateTime recruitmentStartedDate, LocalDateTime recruitmentEndDate) {

            return anyNullExists(companyName, recruitmentStartedDate, recruitmentEndDate) ?
                    null : builder()
                    .companyName(companyName)
                    .recruitmentStartedDate(recruitmentStartedDate)
                    .recruitmentEndDate(recruitmentEndDate)
                    .build();
        }


        private static boolean anyNullExists(String companyName,
                LocalDateTime recruitmentStartedDate,
                LocalDateTime recruitmentEndDate) {


            return companyName == null || recruitmentStartedDate == null
                    || recruitmentEndDate == null;
        }
    }
}
