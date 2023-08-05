package com.plana.infli.web.dto.request.post.create;

import com.plana.infli.domain.PostType;
import com.plana.infli.web.dto.request.post.create.CreatePostServiceRequest.CreateRecruitmentServiceRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Getter
@NoArgsConstructor
public class CreatePostRequest {

    @NotNull(message = "게시판 ID를 입력해주세요")
    private Long boardId;

    @NotNull(message = "게시글 종류를 선택해주세요")
    private PostType postType;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @Nullable
    @Valid
    private CreateRecruitmentRequest recruitment;

    @Builder
    public CreatePostRequest(Long boardId, PostType postType, String title, String content,
            @Nullable CreateRecruitmentRequest recruitment) {

        this.boardId = boardId;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.recruitment = recruitment;
    }


    public CreatePostServiceRequest toServiceRequest(String email) {

        return CreatePostServiceRequest
                .builder()
                .email(email)
                .boardId(boardId)
                .postType(postType)
                .title(title)
                .content(content)
                .recruitment(recruitment != null ? recruitment.toServiceRequest() : null)
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class CreateRecruitmentRequest {

        @NotEmpty(message = "회사명을 입력해주세요")
        private String companyName;

        @NotNull(message = "모집 시작일을 입력해주세요")
        private LocalDateTime startDate;

        @NotNull(message = "모집 종료일을 입력해주세요")
        private LocalDateTime endDate;

        @Builder
        public CreateRecruitmentRequest(String companyName,
                LocalDateTime startDate, LocalDateTime endDate) {

            this.companyName = companyName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public CreateRecruitmentServiceRequest toServiceRequest() {
            return CreateRecruitmentServiceRequest.builder()
                    .companyName(companyName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        }

        public static CreateRecruitmentRequest create(String companyName,
                LocalDateTime startDate, LocalDateTime endDate) {

            return CreateRecruitmentRequest.builder()
                    .companyName(companyName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        }
    }
}

