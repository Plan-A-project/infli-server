package com.plana.infli.web.dto.request.comment.view;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentQueryRequest {

    private final Post post;

    private final Member member;

    private final int size;

    private final int page;

    @Builder
    private CommentQueryRequest(Post post, Member member, int size, int page) {
        this.post = post;
        this.member = member;
        this.size = size;
        this.page = loadPage(page);
    }

    public static CommentQueryRequest of(Post post, Member member, int size, int page) {
        return CommentQueryRequest.builder()
                .post(post)
                .member(member)
                .size(size)
                .page(page)
                .build();
    }

    public static CommentQueryRequest myComments(Member member, int size, int page) {
        return CommentQueryRequest.builder()
                .post(null)
                .member(member)
                .size(size)
                .page(page)
                .build();
    }

    private int loadPage(int page) {
        return Math.max(1, page);
    }

    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
