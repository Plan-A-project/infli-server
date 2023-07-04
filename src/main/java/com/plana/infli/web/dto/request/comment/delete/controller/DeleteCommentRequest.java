package com.plana.infli.web.dto.request.comment.delete.controller;

import com.plana.infli.web.dto.request.comment.delete.service.DeleteCommentServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeleteCommentRequest {

    @NotEmpty(message = "삭제할 댓글 ID가 입력되지 않았습니다")
    List<Long> ids = new ArrayList<>();

    @Builder
    public DeleteCommentRequest(List<Long> ids) {
        this.ids = ids;
    }


    public DeleteCommentServiceRequest toServiceRequest() {
        return DeleteCommentServiceRequest.builder()
                .ids(ids).build();
    }
}
