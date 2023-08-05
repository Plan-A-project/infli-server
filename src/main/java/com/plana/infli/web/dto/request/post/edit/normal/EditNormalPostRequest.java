package com.plana.infli.web.dto.request.post.edit;


import com.plana.infli.web.dto.request.post.edit.EditPostServiceRequest.EditRecruitmentServiceRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditPostRequest {

    @NotNull(message = "글 Id 번호를 입력해주세요")
    private Long postId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @Nullable
    private String thumbnailUrl;

    @Nullable
    @Valid
    private EditRecruitmentRequest recruitment;


    @Builder
    public EditPostRequest(Long postId, String title, String content, String thumbnailUrl,
            EditRecruitmentRequest recruitment) {

        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitment = recruitment;
    }

    public EditPostServiceRequest toServiceRequest(String email) {
        return EditPostServiceRequest.builder()
                .email(email)
                .postId(postId)
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .recruitment(recruitment != null ? recruitment.toServiceRequest() : null)
                .build();
    }


    @Getter
    @NoArgsConstructor
    public static class EditRecruitmentRequest {

        @NotNull(message = "회사명을 입력해주세요")
        private String companyName;

        @NotNull(message = "모집 시작일을 입력해주세요")
        private LocalDateTime startDate;

        @NotNull(message = "모집 종료일을 입력해주세요")
        private LocalDateTime endDate;

        @Builder
        private EditRecruitmentRequest(String companyName, LocalDateTime startDate,
                LocalDateTime endDate) {
            this.companyName = companyName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public EditRecruitmentServiceRequest toServiceRequest() {
            return EditRecruitmentServiceRequest.builder()
                    .companyName(companyName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        }
    }



}
