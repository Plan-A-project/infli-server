package com.plana.infli.web.dto.request.post.view.board;


import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.PostViewOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadPostsByBoardServiceRequest {

    private final PostType type;

    private Integer page;

    private final PostViewOrder order;

    @Builder
    public LoadPostsByBoardServiceRequest(PostType type, Integer page, PostViewOrder order) {
        this.type = type;
        this.page = page;
        this.order = order;
    }

    public void validatePageRequest() {
        if (page == null || page <= 0) {
            page = 1;
        }
    }
}
