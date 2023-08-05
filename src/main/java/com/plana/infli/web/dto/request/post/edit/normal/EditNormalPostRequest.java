package com.plana.infli.web.dto.request.post.edit.normal;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditNormalPostRequest {

    @NotNull(message = "글 Id 번호를 입력해주세요")
    private Long postId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @Nullable
    private String thumbnailUrl;

    @Builder
    public EditNormalPostRequest(Long postId, String title,
            String content, @Nullable String thumbnailUrl) {

        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
    }

    public EditNormalPostServiceRequest toServiceRequest(String email) {
        return EditNormalPostServiceRequest.builder()
                .email(email)
                .postId(postId)
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }


}
