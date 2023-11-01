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
    // 글 작성자
    // 익명글인 경우 : null
    // 나머지 경우 : 작성자 닉네임
    private final String nickname ;

    // 글 내용
    private final String content;

    // 내가 작성한 글인지 여부
    // True -> 지금 글 조회중인 회원이 글 작성자임
    private final boolean isMyPost;

    private final boolean isAdmin;

    private final String profileUrl;

    @Nullable
    private final RecruitmentInfoResponse recruitment;

    @QueryProjection
    public SinglePostResponse(String boardName, Long boardId, String postType,
            @Nullable String nickname,
            Long postId, String title, String content, LocalDateTime createdAt, boolean isMyPost,
            boolean isAdmin, String profileUrl, int viewCount, int likeCount, boolean pressedLike,
            String thumbnailURL, String companyName,
            LocalDateTime recruitmentStartedDate, LocalDateTime recruitmentEndDate) {

        super(postId, title, pressedLike, likeCount, viewCount, createdAt, thumbnailURL);

        this.boardName = boardName;
        this.boardId = boardId;
        this.postType = postType;
        this.nickname = nickname;
        this.content = content;
        this.isMyPost = isMyPost;
        this.isAdmin = isAdmin;
        this.profileUrl = profileUrl;
        this.recruitment = create(companyName, recruitmentStartedDate, recruitmentEndDate);
    }

    @Getter
    public static class RecruitmentInfoResponse {

        private final String companyName;

        private final LocalDateTime startDate;

        private final LocalDateTime endDate;

        @Builder
        public RecruitmentInfoResponse(String companyName, LocalDateTime startDate,
                LocalDateTime endDate) {
            this.companyName = companyName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public static RecruitmentInfoResponse create(String companyName,
                LocalDateTime startDate, LocalDateTime endDate) {

            return allNotNull(companyName, startDate, endDate) ?
                    RecruitmentInfoResponse.builder().companyName(companyName)
                            .startDate(startDate)
                            .endDate(endDate)
                            .build() : null;
        }

        private static boolean allNotNull(String companyName,
                LocalDateTime startDate, LocalDateTime endDate) {

            return companyName != null && startDate != null && endDate != null;
        }
    }
}
