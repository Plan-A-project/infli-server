package com.plana.infli.web.dto.request.post.create.recruitment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRecruitmentPostRequest {

    @NotNull(message = "게시판 ID를 입력해주세요")
    private Long boardId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private String content;

    @NotEmpty(message = "모집 회사명을 입력해주세요")
    private String recruitmentCompanyName;

    @NotNull(message = "모집 시작일을 입력해주세요")
    private LocalDateTime recruitmentStartDate;

    @NotNull(message = "모집 종료일을 입력해주세요")
    private LocalDateTime recruitmentEndDate;

    @Builder
    public CreateRecruitmentPostRequest(Long boardId, String title, String content,
            String recruitmentCompanyName, LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }


    public CreateRecruitmentPostServiceRequest toServiceRequest(String username) {
        return CreateRecruitmentPostServiceRequest.builder()
                .username(username)
                .boardId(boardId)
                .title(title)
                .content(content)
                .recruitmentCompanyName(recruitmentCompanyName)
                .recruitmentStartDate(recruitmentStartDate)
                .recruitmentEndDate(recruitmentEndDate)
                .build();
    }

}
