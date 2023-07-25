package com.plana.infli.web.dto.response.post.board.notice;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public class AnnouncementPost extends DefaultPost {


    @QueryProjection
    public AnnouncementPost(Long postId, String title, int likeCount, int viewCount,
            LocalDateTime createdAt, String thumbnailURL) {
        super(postId, title, likeCount, viewCount, createdAt, thumbnailURL);
    }
}
