package com.plana.infli.web.dto.request.comment.view.post;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCommentsInPostRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long id;

    private String page;

    @Builder
    public LoadCommentsInPostRequest(Long id, String page) {
        this.id = id;
        this.page = page;
    }

    public LoadCommentsInPostServiceRequest toServiceRequest(String email) {
        return LoadCommentsInPostServiceRequest.builder()
                .email(email)
                .id(id)
                .page(page)
                .build();
    }
}
