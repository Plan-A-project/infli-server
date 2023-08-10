package com.plana.infli.web.dto.request.comment.view.post;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class LoadCommentsInPostRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long id;

    @NotNull(message = "페이지 정보를 입력해주세요")
    private Integer page;

    @Builder
    public LoadCommentsInPostRequest(Long id, Integer page) {
        this.id = id;
        this.page = page;
    }

    public LoadCommentsInPostServiceRequest toServiceRequest(String username) {
        return LoadCommentsInPostServiceRequest.builder()
                .username(username)
                .id(id)
                .page(page)
                .build();
    }

}
