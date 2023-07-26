package com.plana.infli.web.dto.response.post.search;

import java.util.List;
import lombok.Getter;

@Getter
public class SearchedPostsResponse {


    // 한 페이지당 조회되길 희망하는 글 갯수
    // 기본값 : 20개씩 조회
    private final int sizeRequest;

    // 현재 페이지에 실제로 조회된 글의 갯수
    private final int actualSize;

    // 현재 페이지
    private final int currentPage;

    // 검색된 글 목록
    private final List<SearchedPost> posts;

    public SearchedPostsResponse(int sizeRequest, int actualSize, int currentPage,
            List<SearchedPost> posts) {
        this.sizeRequest = sizeRequest;
        this.actualSize = actualSize;
        this.currentPage = currentPage;
        this.posts = posts;
    }

}
