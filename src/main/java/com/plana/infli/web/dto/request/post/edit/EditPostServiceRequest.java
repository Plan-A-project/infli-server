package com.plana.infli.web.dto.request.post.edit;

import com.plana.infli.web.dto.request.post.edit.EditPostRequest.RecruitmentInfo;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditPostServiceRequest {

    private final Long postId;

    private final String title;

    private final String content;

    @Nullable
    private final String thumbnailUrl;

    @Nullable
    private final RecruitmentInfo recruitmentInfo;


    @Builder
    public EditPostServiceRequest(Long postId, String title, String content, String thumbnailUrl,
            RecruitmentInfo recruitmentInfo) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitmentInfo = recruitmentInfo;
    }
}
