package com.plana.infli.web.dto.request.post.view.board;



import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.view.PostQueryRequest.PostViewOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoadPostsByBoardRequest {

    @NotNull(message = "글 종류를 선택해주세요")
    private PostType type;

    private Integer page;

    @NotNull(message = "최신순, 인기순 여부를 선택해주세요")
    private PostViewOrder order;

    @Builder
    public LoadPostsByBoardRequest(PostType type, Integer page, PostViewOrder order) {
        this.type = type;
        this.page = page;
        this.order = order;
    }

    public LoadPostsByBoardServiceRequest toServiceRequest(Long boardId, String username) {
        return LoadPostsByBoardServiceRequest.builder()
                .username(username)
                .boardId(boardId)
                .type(type)
                .order(order)
                .page(page)
                .build();
    }
}
