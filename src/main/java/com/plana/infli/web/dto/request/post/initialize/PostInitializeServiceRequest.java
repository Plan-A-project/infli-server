package com.plana.infli.web.dto.request.post.initialize;

import com.plana.infli.domain.PostType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostInitializeServiceRequest {

    private final Long boardId;

    private final PostType postType;

    @Builder
    public PostInitializeServiceRequest(Long boardId, PostType postType) {
        this.boardId = boardId;
        this.postType = postType;
    }
}
