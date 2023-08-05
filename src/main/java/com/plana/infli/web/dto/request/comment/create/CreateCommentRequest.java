package com.plana.infli.web.dto.request.comment.create.controller;


import com.plana.infli.web.dto.request.comment.create.service.CreateCommentServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "글 번호가 입력되지 않았습니다")
    private Long postId;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    @Nullable
    private Long parentCommentId;

    @Builder
    private CreateCommentRequest(Long postId, @Nullable Long parentCommentId, String content) {
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
    }

    public CreateCommentServiceRequest toServiceRequest(String email) {
        return CreateCommentServiceRequest.builder()
                .email(email)
                .postId(postId)
                .parentCommentId(parentCommentId)
                .content(content)
                .build();
    }

}
