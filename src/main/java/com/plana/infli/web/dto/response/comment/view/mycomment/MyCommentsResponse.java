package com.plana.infli.web.dto.response.comment.view.mycomment;

import com.plana.infli.web.dto.request.comment.view.CommentQueryRequest;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;

@Getter
public class MyCommentsResponse {

    private final int sizeRequest;

    private final int actualSize;

    private final int currentPage;

    private final List<MyComment> comments;

    @Builder
    public MyCommentsResponse(int sizeRequest, int actualSize, int currentPage,
            List<MyComment> comments) {
        this.sizeRequest = sizeRequest;
        this.actualSize = actualSize;
        this.currentPage = currentPage;
        this.comments = comments;
    }

    public static MyCommentsResponse of(CommentQueryRequest request, List<MyComment> comments) {

        return MyCommentsResponse.builder()
                .sizeRequest(request.getSize())
                .actualSize(comments != null ? comments.size() : 0)
                .currentPage(request.getPage())
                .comments(comments)
                .build();
    }

}
