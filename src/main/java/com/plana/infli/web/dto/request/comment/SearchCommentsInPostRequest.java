package com.plana.infli.web.dto.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchCommentsInPostRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private final Long id;

    private final Integer page;

    @Builder
    public SearchCommentsInPostRequest(Long id, Integer page) {
        this.id = id;
        this.page = page;
    }
}
