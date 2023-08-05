package com.plana.infli.web.dto.request.comment.view.post.service;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCommentsInPostServiceRequest {

    private final String email;

    private final Long id;

    private final String page;

    @Builder
    public LoadCommentsInPostServiceRequest(String email, Long id, String page) {
        this.email = email;
        this.id = id;
        this.page = page;
    }
}
