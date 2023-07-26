package com.plana.infli.web.dto.request.post.view.board;


import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadPostsByBoardServiceRequest {

    private final String email;

    private final Long boardId;

    private final PostType type;

    private final String page;

    private final PostViewOrder order;

    @Builder
    public LoadPostsByBoardServiceRequest(String email, Long boardId,
            PostType type, String page, PostViewOrder order) {
        this.email = email;
        this.boardId = boardId;
        this.type = type;
        this.page = page;
        this.order = order;
    }
}
