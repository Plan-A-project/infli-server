package com.plana.infli.web.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class EditCommentRequest {

    @NotNull(message = "수정할 댓글번호가 입력되지 않았습니다")
    private Long commentId;

    @NotBlank(message = "내용을 입력하지 않았습니다")
    @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요")
    private String content;

    @Builder
    public EditCommentRequest(Long commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }
}
