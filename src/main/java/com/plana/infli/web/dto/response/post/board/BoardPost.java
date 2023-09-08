package com.plana.infli.web.dto.response.post.board;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardPost extends DefaultPost {

    private final String memberRole;

    private final String companyName;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;


    @Builder
    @QueryProjection
    public BoardPost(Long postId, String title, LocalDateTime createdAt,
            String thumbnailURL, String memberRole, int likeCount, boolean pressedLike,int viewCount,
            String companyName, LocalDateTime recruitmentStartDate,
            LocalDateTime recruitmentEndDate) {
        super(postId, title, pressedLike, likeCount, viewCount, createdAt, thumbnailURL);

        this.memberRole = memberRole;
        this.companyName = companyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }
}
