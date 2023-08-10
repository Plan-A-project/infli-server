package com.plana.infli.web.dto.request.post.edit.recruitment;

import static com.plana.infli.domain.embedded.post.Recruitment.*;

import com.plana.infli.domain.embedded.post.Recruitment;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditRecruitmentPostServiceRequest {

    private final String username;

    private final Long postId;

    private final String title;

    private final String content;

    @Nullable
    private final String thumbnailUrl;

    private final String recruitmentCompanyName;

    private final LocalDateTime recruitmentStartDate;

    private final LocalDateTime recruitmentEndDate;

    @Builder
    public EditRecruitmentPostServiceRequest(String username, Long postId, String title,
            String content, @Nullable String thumbnailUrl, String recruitmentCompanyName,
            LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {

        this.username = username;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitmentCompanyName = recruitmentCompanyName;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }

    public static Recruitment of(EditRecruitmentPostServiceRequest request) {
        return create(request.recruitmentCompanyName,
                request.recruitmentStartDate, request.recruitmentEndDate);
    }

}
