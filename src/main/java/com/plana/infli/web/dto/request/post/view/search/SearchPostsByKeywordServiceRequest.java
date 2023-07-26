package com.plana.infli.web.dto.request.post.view.search;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchPostsByKeywordServiceRequest {

    private final String email;

    private final String keyword;

    private final String page;

    @Builder
    public SearchPostsByKeywordServiceRequest(String email, String keyword, String page) {
        this.email = email;
        this.keyword = keyword;
        this.page = page;
    }
}
