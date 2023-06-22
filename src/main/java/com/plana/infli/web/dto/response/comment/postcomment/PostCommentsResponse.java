package com.plana.infli.web.dto.response.comment.postcomment;

import com.plana.infli.web.dto.request.comment.SearchCommentsInPostRequest;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostCommentsResponse {

    private final Long postId;

    private final Long totalComments;

    private final Integer sizeRequest = 100;

    private final Integer actualSize;

    private final Integer currentPage;

    private final boolean isAdmin;

    private final List<PostComment> comments;

    @Builder
    private PostCommentsResponse(Long postId, Long totalComments, Integer actualSize,
            Integer currentPage, List<PostComment> comments) {
        this.postId = postId;
        this.totalComments = totalComments;
        this.actualSize = actualSize;
        this.currentPage = currentPage;
        this.isAdmin = isAdmin();
        this.comments = comments;
    }

    public static PostCommentsResponse createPostCommentsResponse(
            SearchCommentsInPostRequest request, List<PostComment> comments,
            Long totalComments) {

        return PostCommentsResponse.builder()
                .postId(request.getId())
                .totalComments(totalComments)
                .actualSize(comments.size())
                .currentPage(request.getPage())
                .comments(comments)
                .build();
    }
}
