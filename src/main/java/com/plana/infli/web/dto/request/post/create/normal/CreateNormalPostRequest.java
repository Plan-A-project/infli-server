package com.plana.infli.web.dto.request.post.create.normal;

import com.plana.infli.domain.PostType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateNormalPostRequest {

    @NotNull(message = "게시판 ID를 입력해주세요")
    private Long boardId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @NotNull(message = "글 종류를 선택해주세요")
    private PostType postType;

    @Builder
    public CreateNormalPostRequest(Long boardId,
            String title, String content, PostType postType) {

        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.postType = postType;
    }


    public CreateNormalPostServiceRequest toServiceRequest(String username) {

        return CreateNormalPostServiceRequest
                .builder()
                .username(username)
                .boardId(boardId)
                .title(title)
                .content(content)
                .postType(postType)
                .build();
    }


}

