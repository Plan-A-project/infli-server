package com.plana.infli.web.dto.response.post.board;

import com.plana.infli.web.dto.request.post.view.PostQueryRequest;
import com.plana.infli.web.dto.response.post.DefaultPost;
import com.plana.infli.web.dto.response.post.DefaultPostsResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardPostsResponse extends DefaultPostsResponse {

    private final Long boardId;

    private final String boardName;

    @Builder
    public BoardPostsResponse(Long boardId, String boardName, int sizeRequest, int actualSize,
            int currentPage, List<? extends DefaultPost> posts) {
        super(sizeRequest, actualSize, currentPage, posts);
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public static BoardPostsResponse loadResponse(List<BoardPost> posts,
            PostQueryRequest request) {

        return BoardPostsResponse.builder()
                .boardId(request.getBoard().getId())
                .boardName(request.getBoard().getBoardName())
                .sizeRequest(request.getSize())
                .actualSize(posts.size())
                .currentPage(request.getPage())
                .posts(posts)
                .build();
    }
}
