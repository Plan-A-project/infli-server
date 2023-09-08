package com.plana.infli.web.dto.request.comment.edit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class EditCommentRequest {

    @NotNull(message = "수정할 댓글번호가 입력되지 않았습니다")
    private Long commentId;

    @NotBlank(message = "내용을 입력하지 않았습니다")
    private String content;

    @Builder
    private EditCommentRequest(Long commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }

    public EditCommentServiceRequest toServiceRequest(String username) {

        return EditCommentServiceRequest.builder()
                .username(username)
                .commentId(commentId)
                .content(content)
                .build();
    }

}
