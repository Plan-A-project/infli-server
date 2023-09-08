package com.plana.infli.web.dto.request.post.view.board;


import com.plana.infli.domain.type.PostType;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadPostsByBoardServiceRequest {

    private final String username;

    private final Long boardId;

    private final PostType type;

    private final Integer page;

    private final PostViewOrder order;

    private final Integer size;

    @Builder
    private LoadPostsByBoardServiceRequest(String username, Long boardId,
            PostType type, Integer page, PostViewOrder order, Integer size) {

        this.username = username;
        this.boardId = boardId;
        this.type = type;
        this.page = page;
        this.order = order;
        this.size = size;
    }

}
