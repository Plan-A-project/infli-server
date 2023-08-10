package com.plana.infli.web.dto.request.post.view.search;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchPostsByKeywordRequest {

    @NotEmpty(message = "검색어를 입력해주세요")
    private String keyword;

    @NotNull(message = "페이지 정보를 입력해주세요")
    private Integer page;

    @Builder
    private SearchPostsByKeywordRequest(String keyword, Integer page) {
        this.keyword = keyword;
        this.page = page;
    }

    public SearchPostsByKeywordServiceRequest toServiceRequest(String username) {
        return SearchPostsByKeywordServiceRequest.builder()
                .username(username)
                .keyword(keyword)
                .page(page)
                .build();
    }

}
