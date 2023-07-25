package com.plana.infli.web.dto.request.post.initialize;

import com.plana.infli.domain.PostType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostInitializeRequest {

    @NotNull(message = "게시판 ID를 입력해주세요")
    private final Long boardId;

    @NotNull(message = "게시글 종류를 선택해주세요")
    private final PostType postType;

    @Builder
    public PostInitializeRequest(Long boardId, PostType postType) {
        this.boardId = boardId;
        this.postType = postType;
    }

    public PostInitializeServiceRequest toServiceRequest() {
        return PostInitializeServiceRequest
                .builder()
                .boardId(boardId)
                .postType(postType)
                .build();
    }
}
