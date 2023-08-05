package com.plana.infli.web.dto.request.comment.delete;

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
    private List<Long> ids;


    @Builder
    public DeleteCommentRequest(List<Long> ids) {
        this.ids = ids != null ? ids : new ArrayList<>();
    }


    public DeleteCommentServiceRequest toServiceRequest(String email) {
        return DeleteCommentServiceRequest.builder()
                .email(email)
                .ids(ids).build();
    }

}
