package com.plana.infli.web.dto.request.post.search.service;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchPostsByKeywordServiceRequest {

    private String keyword;

    private String page;

    @Builder
    public SearchPostsByKeywordServiceRequest(String keyword, String page) {
        this.keyword = keyword;
        this.page = page;
    }
}
