package com.plana.infli.web.dto.response.post.my;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MyPost extends DefaultPost {

    private final String boardName;

    @QueryProjection
    public MyPost(Long postId, String title, int likeCount, int viewCount, LocalDateTime createdAt,
            String thumbnailURL, String boardName) {
        super(postId, title, likeCount, viewCount, createdAt, thumbnailURL);
        this.boardName = boardName;
    }
}
