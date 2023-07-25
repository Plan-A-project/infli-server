package com.plana.infli.web.dto.response.post;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardPostDTO extends DefaultPost {

    private final String companyName;

    private final String memberRole;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;

    @Builder
    @QueryProjection
    public BoardPostDTO(Long postId, String title, LocalDateTime createdAt,
            String thumbnailURL, String memberRole, int likeCount, int viewCount,
            String companyName, LocalDateTime recruitmentStartDate,
            LocalDateTime recruitmentEndDate) {
        super(postId, title, likeCount, viewCount, createdAt, thumbnailURL);

        this.memberRole = memberRole;
        this.companyName = companyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }
}
