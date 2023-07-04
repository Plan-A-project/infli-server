package com.plana.infli.web.dto.request.comment.view.post.service;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCommentsInPostServiceRequest {

    private final Long id;

    private final Integer page;

    @Builder
    public LoadCommentsInPostServiceRequest(Long id, Integer page) {
        this.id = id;
        this.page = page;
    }
}
