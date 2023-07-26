package com.plana.infli.web.dto.request.post.view.board;


import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadPostsByBoardRequest {

    @NotNull(message = "글 종류를 선택해주세요")
    private final PostType type;

    private final String page;

    @NotNull(message = "최신순, 인기순 여부를 선택해주세요")
    private final PostQueryRequest.PostViewOrder order;

    @Builder
    public LoadPostsByBoardRequest(PostType type, String page, PostViewOrder order) {
        this.type = type;
        this.page = page;
        this.order = order;
    }


    public LoadPostsByBoardServiceRequest toServiceRequest(Long boardId, String email) {
        return LoadPostsByBoardServiceRequest.builder()
                .email(email)
                .boardId(boardId)
                .type(type)
                .order(order)
                .page(page)
                .build();
    }
}
