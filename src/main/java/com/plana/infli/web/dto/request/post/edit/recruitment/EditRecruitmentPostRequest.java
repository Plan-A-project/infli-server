package com.plana.infli.web.dto.request.post.edit.recruitment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Bag;

@Getter
@NoArgsConstructor
public class EditRecruitmentPostRequest {

    @NotNull(message = "글 Id 번호를 입력해주세요")
    private Long postId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @Nullable
    private String thumbnailUrl;

    @NotNull(message = "회사명을 입력해주세요")
    private String recruitmentCompanyName;

    @NotNull(message = "모집 시작일을 입력해주세요")
    private LocalDateTime recruitmentStartDate;

    @NotNull(message = "모집 종료일을 입력해주세요")
    private LocalDateTime recruitmentEndDate;

    @Builder
    public EditRecruitmentPostRequest(Long postId, String title, String content,
            @Nullable String thumbnailUrl, String recruitmentCompanyName,
            LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {

        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }

    public EditRecruitmentPostServiceRequest toServiceRequest(String username) {
        return EditRecruitmentPostServiceRequest.builder()
                .username(username)
                .postId(postId)
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .recruitmentCompanyName(recruitmentCompanyName)
                .recruitmentStartDate(recruitmentStartDate)
                .recruitmentEndDate(recruitmentEndDate)
                .build();
    }

}
