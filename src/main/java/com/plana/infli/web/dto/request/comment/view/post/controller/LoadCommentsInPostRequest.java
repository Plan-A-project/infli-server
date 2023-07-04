package com.plana.infli.web.dto.request.comment.view.post.controller;

import com.plana.infli.web.dto.request.comment.view.post.service.LoadCommentsInPostServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCommentsInPostRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private final Long id;

    private final Integer page;


    @Builder
    public LoadCommentsInPostRequest(Long id, Integer page) {
        this.id = id;
        this.page = page;
    }

    public LoadCommentsInPostServiceRequest toServiceRequest() {
        return LoadCommentsInPostServiceRequest.builder()
                .id(id)
                .page(page)
                .build();
    }
}
