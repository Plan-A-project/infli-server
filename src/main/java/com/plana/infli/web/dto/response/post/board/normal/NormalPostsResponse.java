package com.plana.infli.web.dto.response.post.board.normal;

import com.plana.infli.web.dto.response.post.DefaultPost;
import com.plana.infli.web.dto.response.post.DefaultPostsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NormalPostsResponse extends DefaultPostsResponse {

    private final Long boardId;

    private final String boardName;

    @Builder
    public NormalPostsResponse(int sizeRequest, int actualSize, int currentPage,
            List<? extends DefaultPost> posts, Long boardId, String boardName) {

        super(sizeRequest, actualSize, currentPage, posts);
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public static NormalPostsResponse loadNormalDefaultPostsResponse(List<NormalPost> posts,
            int page) {

        return NormalPostsResponse.builder()
                .sizeRequest(20)
                .actualSize(posts.size())
                .currentPage(page)
                .posts(posts)
                .build();
    }
}
