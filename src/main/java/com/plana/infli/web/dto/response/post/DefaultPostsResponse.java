package com.plana.infli.web.dto.response.post;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public abstract class DefaultPostsResponse {

    private final int sizeRequest;

    private final int actualSize;

    private final int currentPage;

    private final List<? extends DefaultPost> posts;

    public DefaultPostsResponse(int sizeRequest, int currentPage,
            int actualSize, List<? extends DefaultPost> posts) {

        this.sizeRequest = sizeRequest;
        this.currentPage = currentPage;
        this.posts = posts != null ? posts : new ArrayList<>();
        this.actualSize = actualSize;
    }
}
