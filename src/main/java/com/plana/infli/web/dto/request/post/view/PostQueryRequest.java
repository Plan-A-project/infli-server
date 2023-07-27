package com.plana.infli.web.dto.request.post.view;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.board.LoadPostsByBoardServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class PostQueryRequest {

    private final Post post;

    private final Member member;

    private final Board board;

    private final int page;

    private final int size;

    private final PostType type;

    private final PostViewOrder viewOrder;

    private final String keyword;

    @Builder
    public PostQueryRequest(Post post, Member member, Board board, String page, int size,
            PostType type, PostViewOrder viewOrder, String keyword) {

        this.post = post;
        this.member = member;
        this.board = board;
        this.page = convert(page);
        this.size = size;
        this.type = type;
        this.viewOrder = viewOrder;
        this.keyword = keyword;
    }

    public static PostQueryRequest singlePost(Post post, Member member) {
        return PostQueryRequest.builder()
                .post(post)
                .member(member)
                .build();
    }

    public static PostQueryRequest myPosts(Member member, String page, int size) {
        return PostQueryRequest.builder()
                .member(member)
                .page(page)
                .size(size)
                .build();
    }

    public static PostQueryRequest postsByBoard(Board board, Member member,
            LoadPostsByBoardServiceRequest request, int size) {

        return PostQueryRequest.builder()
                .board(board)
                .member(member)
                .size(size)
                .page(request.getPage())
                .viewOrder(request.getOrder())
                .type(request.getType())
                .build();
    }

    public static PostQueryRequest searchByKeyword(Member member,
            String keyword, String page, int size) {

        return PostQueryRequest.builder()
                .member(member)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();
    }

    @Getter
    @RequiredArgsConstructor
    public enum PostViewOrder {
        recent("최신순"),

        popular("인기순"),
        ;

        private final String value;
    }


    private int convert(String page) {
        try {
            return Math.max(1, Integer.parseInt(page));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
