package com.plana.infli.web.dto.request.post.edit;


import static com.plana.infli.web.dto.request.post.edit.EditPostRequest.RecruitmentInfo.*;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditPostRequest {

    @NotNull(message = "글 Id 번호를 입력해주세요")
    private final Long postId;

    @NotEmpty(message = "글 제목을 입력해주세요")
    private final String title;

    @NotEmpty(message = "글 내용을 입력해주세요")
    private final String content;

    @Nullable
    private final String thumbnailUrl;

    @Nullable
    private final RecruitmentInfo recruitmentInfo;


    @Builder
    public EditPostRequest(Long postId, String title, String content, String thumbnailUrl,
            LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate, String recruitmentCompanyName) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitmentInfo = create(recruitmentCompanyName, recruitmentStartDate,
                recruitmentEndDate);
    }

    @Getter
    public static class RecruitmentInfo {

        private final String companyName;

        private final LocalDateTime startDate;

        private final LocalDateTime endDate;

        @Builder
        private RecruitmentInfo(String companyName, LocalDateTime recruitmentStartDate,
                LocalDateTime recruitmentEndDate) {
            this.companyName = companyName;
            this.startDate = recruitmentStartDate;
            this.endDate = recruitmentEndDate;
        }


        public static RecruitmentInfo create(String companyName, LocalDateTime recruitmentStartDate,
                LocalDateTime recruitmentEndDate) {

            if (allNull(companyName, recruitmentStartDate, recruitmentEndDate)) {
                return null;
            }

            return new RecruitmentInfo(companyName, recruitmentStartDate, recruitmentEndDate);
        }

        private static boolean allNull(String companyName, LocalDateTime recruitmentStartDate,
                LocalDateTime recruitmentEndDate) {
            return companyName == null && recruitmentStartDate == null
                    && recruitmentEndDate == null;
        }
    }


    //TODO 코드 리팩토링 필요
    public EditPostServiceRequest toServiceRequest() {
        return EditPostServiceRequest.builder()
                .postId(postId)
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .recruitmentInfo(recruitmentInfo)
                .build();
    }
}
