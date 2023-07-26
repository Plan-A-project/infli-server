package com.plana.infli.web.dto.request.comment.edit.controller;

import com.plana.infli.web.dto.request.comment.edit.service.EditCommentServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class EditCommentRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long postId;

    @NotNull(message = "수정할 댓글번호가 입력되지 않았습니다")
    private Long commentId;

    @NotBlank(message = "내용을 입력하지 않았습니다")
    private String content;

    @Builder
    private EditCommentRequest(Long postId, Long commentId, String content) {
        this.postId = postId;
        this.commentId = commentId;
        this.content = content;
    }

    public EditCommentServiceRequest toServiceRequest(String email) {

        return EditCommentServiceRequest.builder()
                .email(email)
                .postId(postId)
                .commentId(commentId)
                .content(content)
                .build();
    }

}
