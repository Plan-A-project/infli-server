package com.plana.infli.web.dto.request.post.edit.normal;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditPostServiceRequest {

    private final String email;

    private final Long postId;

    private final String title;

    private final String content;

    @Nullable
    private final String thumbnailUrl;

    @Nullable
    private final EditRecruitmentServiceRequest recruitment;


    @Builder
    public EditPostServiceRequest(String email, Long postId, String title, String content,
            String thumbnailUrl, EditRecruitmentServiceRequest recruitment) {
        this.email = email;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitment = recruitment;
    }

    @Getter
    public static class EditRecruitmentServiceRequest {

        private final String companyName;

        private final LocalDateTime startDate;

        private final LocalDateTime endDate;

        @Builder
        public EditRecruitmentServiceRequest(String companyName, LocalDateTime startDate,
                LocalDateTime endDate) {
            this.companyName = companyName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

}
