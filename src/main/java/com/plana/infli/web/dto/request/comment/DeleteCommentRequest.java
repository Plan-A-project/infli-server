package com.plana.infli.web.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeleteCommentRequest {

    @NotBlank(message = "삭제할 댓글 ID가 입력되지 않았습니다")
    List<Long> ids = new ArrayList<>();

    @Builder
    public DeleteCommentRequest(List<Long> ids) {
        this.ids = ids;
    }
}
