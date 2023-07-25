package com.plana.infli.web.dto.response.post;

import com.plana.infli.domain.Board;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsByBoardResponse extends DefaultPostsResponse{

    private final Long boardId;

    private final String boardName;

    @Builder
    public PostsByBoardResponse(Long boardId, String boardName, int sizeRequest, int actualSize,
            int currentPage, List<? extends DefaultPost> posts) {
        super(sizeRequest, actualSize, currentPage, posts);
        this.boardId = boardId;
        this.boardName = boardName;
    }

    public static PostsByBoardResponse loadResponse(List<BoardPostDTO> posts, int page,
            Board board) {
        return PostsByBoardResponse.builder()
                .boardId(board.getId())
                .boardName(board.getBoardName())
                .sizeRequest(20)
                .actualSize(posts.size())
                .currentPage(page)
                .posts(posts)
                .build();
    }
}
