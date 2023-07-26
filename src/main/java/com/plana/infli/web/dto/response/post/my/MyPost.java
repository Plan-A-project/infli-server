package com.plana.infli.web.dto.response.post.my;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MyPost extends DefaultPost {

    private final String boardName;

    @Nullable
    private final String companyName;

    @Nullable
    private final LocalDateTime recruitmentStartDate;

    @Nullable
    private final LocalDateTime recruitmentEndDate;

    @QueryProjection
    public MyPost(Long postId, String title, boolean pressedLike,int likeCount, int viewCount, LocalDateTime createdAt,
            String thumbnailURL, String boardName, String companyName,
            LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {
        super(postId, title, pressedLike, likeCount, viewCount, createdAt, thumbnailURL);

        this.boardName = boardName;
        this.companyName = companyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }
}
