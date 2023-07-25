package com.plana.infli.web.dto.response.post.board.normal;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class NormalPost extends DefaultPost {

    @QueryProjection
    public NormalPost(Long postId, String title, int likeCount, int viewCount,
            LocalDateTime createdAt, String thumbnailURL) {
        super(postId, title, likeCount, viewCount, createdAt, thumbnailURL);
    }
}
