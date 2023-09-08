package com.plana.infli.web.dto.response.post.search;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SearchedPost extends DefaultPost {

    private final String content;

    private final String boardName;

    private final Long boardId;

    @QueryProjection
    public SearchedPost(Long postId, String title, int likeCount, boolean pressedLike,
            int viewCount, LocalDateTime createdAt, String thumbnailURL,
            String content, String boardName, Long boardId) {

        super(postId, title, pressedLike, likeCount, viewCount, createdAt, thumbnailURL);
        this.content = content;
        this.boardName = boardName;
        this.boardId = boardId;
    }
}
