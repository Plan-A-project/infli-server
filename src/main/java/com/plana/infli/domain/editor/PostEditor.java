package com.plana.infli.domain.editor;

import static com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest.of;

import com.plana.infli.domain.Post;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.web.dto.request.post.edit.normal.EditNormalPostServiceRequest;
import com.plana.infli.web.dto.request.post.edit.recruitment.EditRecruitmentPostServiceRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEditor {

    private final String title;

    private final String content;

    private final String thumbnailUrl;

    private final Recruitment recruitment;

    private final boolean isDeleted;

    private final int viewCount;


    @Builder
    public PostEditor(String title, String content, String thumbnailUrl, Recruitment recruitment,
            boolean isDeleted, int viewCount) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.recruitment = recruitment;
        this.isDeleted = isDeleted;
        this.viewCount = viewCount;
    }


    public static void edit(EditNormalPostServiceRequest request, Post post) {

        post.edit(post.toEditor()
                .title(request.getTitle())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .build());
    }

    public static void edit(EditRecruitmentPostServiceRequest request, Post post) {

        post.edit(post.toEditor()
                .title(request.getTitle())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .recruitment(of(request))
                .build());
    }

    public static void delete(Post post) {
        post.edit(post.toEditor()
                .isDeleted(true)
                .build());
    }

    public static void increaseViewCount(Post post) {
        int currentViewCount = post.getViewCount();

        int increasedViewCount = currentViewCount + 1;

        post.edit(post.toEditor()
                .viewCount(increasedViewCount)
                .build());
    }
}
