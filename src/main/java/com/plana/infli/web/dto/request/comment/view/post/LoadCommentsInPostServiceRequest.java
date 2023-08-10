package com.plana.infli.web.dto.request.comment.view.post;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCommentsInPostServiceRequest {

    private final String username;

    private final Long id;

    private final Integer page;

    @Builder
    public LoadCommentsInPostServiceRequest(String username, Long id, Integer page) {
        this.username = username;
        this.id = id;
        this.page = page;
    }
}
