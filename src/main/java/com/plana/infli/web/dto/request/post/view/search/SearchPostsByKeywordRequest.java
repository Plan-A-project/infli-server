package com.plana.infli.web.dto.request.post.view.search;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchPostsByKeywordRequest {

    @NotEmpty(message = "검색어를 입력해주세요")
    private String keyword;

    private String page;

    @Builder
    private SearchPostsByKeywordRequest(String keyword, String page) {
        this.keyword = keyword;
        this.page = page;
    }

    public SearchPostsByKeywordServiceRequest toServiceRequest(String email) {
        return SearchPostsByKeywordServiceRequest.builder()
                .email(email)
                .keyword(keyword)
                .page(page)
                .build();
    }

}
