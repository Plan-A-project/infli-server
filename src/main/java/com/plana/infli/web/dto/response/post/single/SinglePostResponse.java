package com.plana.infli.web.dto.response.post;

import com.plana.infli.domain.Post;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
public class SinglePostResponse {

    //글 ID
    private Long id;

    //작성자 ID
    private Long userId;

    //작성자 이름
    private String nickname;

    private String boardName;
    private String title;
    private String main;
    private String thumbnailUrl;

    // 조회수
    private int viewCount = 0;

    // 좋아요 수
    private int likeCount = 0;

    // 해당 글에 댓글을 작성한 회원의 갯수
    private int commentMemberCount = 0;

    //기업명
    @Nullable
    private String enterprise;

    @Nullable
    private LocalDate startDate;

    @Nullable
    private LocalDate endDate;

    public SinglePostResponse(Post post) {
        this.id = post.getId();
        this.userId = post.getMember().getId();
        this.nickname = post.getMember().getNickname();
        this.boardName = post.getBoard().getBoardName();
        this.title = post.getTitle();
        this.main = post.getContent();
        this.enterprise = post.getEnterprise();
        this.startDate = post.getRecruitmentStartDate();
        this.endDate = post.getRecruitmentEndDate();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getViewCount();
        this.commentMemberCount = post.getCommentMemberCount();
        if (!post.getImageList().isEmpty()) {
            this.thumbnailUrl = post.getImageList().get(0).getImageUrl();
        }
    }

}
