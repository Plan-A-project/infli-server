package com.plana.infli.web.dto.request.post.view.search;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchPostsByKeywordServiceRequest {

    private final String username;

    private final String keyword;

    private final Integer page;

    @Builder
    public SearchPostsByKeywordServiceRequest(String username, String keyword, Integer page) {
        this.username = username;
        this.keyword = keyword;
        this.page = page;
    }
}
