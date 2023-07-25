package com.plana.infli.domain.editor.post;

import static com.plana.infli.domain.embeddable.Recruitment.*;

import com.plana.infli.domain.Post;
import com.plana.infli.domain.embeddable.Recruitment;
import com.plana.infli.web.dto.request.post.edit.EditPostServiceRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEditor {

    private final String title;

    private final String content;

    private final String thumbnailUrl;

    private final boolean isPublished;

    private final Recruitment recruitment;

    @Builder
    public PostEditor(String title, String content, String thumbnailUrl, boolean isPublished,
            Recruitment recruitment) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.isPublished = isPublished;
        this.recruitment = recruitment;
    }

    public static void editPost(EditPostServiceRequest request, Post post) {


        Recruitment newRecruitment = createNewRecruitment(request);


        post.edit(post.toEditor()
                .title(request.getTitle())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .isPublished(true)
                .recruitment(newRecruitment)
                .build());



    }

    private static Recruitment createNewRecruitment(EditPostServiceRequest request) {

        if (request.getRecruitmentInfo() == null) {
            return null;
        }

        return create(
                request.getRecruitmentInfo().getCompanyName(),
                request.getRecruitmentInfo().getStartDate(),
                request.getRecruitmentInfo().getEndDate());
    }

}
