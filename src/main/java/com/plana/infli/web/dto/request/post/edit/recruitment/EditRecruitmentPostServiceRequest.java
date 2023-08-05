package com.plana.infli.web.dto.request.post.edit.recruitment;

import com.plana.infli.domain.embeddable.Recruitment;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditRecruitmentPostServiceRequest {

    private final String email;

    private final Long postId;

    private final String title;

    private final String content;

    @Nullable
    private final String thumbnailUrl;

    private final String recruitmentCompanyName;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;

    @Builder
    public EditRecruitmentPostServiceRequest(String email, Long postId, String title, String content,
            @Nullable String thumbnailUrl, String recruitmentCompanyName,
            LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {
        this.email = email;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }

    public static Recruitment of(EditRecruitmentPostServiceRequest request) {
        return Recruitment.create(request.recruitmentCompanyName,
                request.recruitmentStartDate, request.recruitmentEndDate);
    }
}
