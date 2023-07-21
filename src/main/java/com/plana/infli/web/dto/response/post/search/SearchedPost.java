package com.plana.infli.web.dto.response.post.search;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SearchedPost {

    private final Long id;

    private final String title;

    private final String content;

    private final LocalDateTime createdAt;

    private int postLikesCounts;

    private int commentsCounts;

    private final int viewCounts;


    @QueryProjection
    public SearchedPost(Long id, String title, String content, LocalDateTime createdAt, int viewCounts) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.viewCounts = viewCounts;
    }

    public void setPostLikesCounts(int postLikesCounts) {
        this.postLikesCounts = postLikesCounts;
    }

    public void setCommentsCounts(int commentsCounts) {
        this.commentsCounts = commentsCounts;
    }
}
