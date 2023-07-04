package com.plana.infli.web.dto.response.board.view;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PopularBoardsResponse {

    // 게시판 목록
    private final List<SinglePopularBoard> boards;

    @Builder
    private PopularBoardsResponse(List<SinglePopularBoard> boards) {
        this.boards = boards != null ? boards : new ArrayList<>();
    }

    public static PopularBoardsResponse createPopularBoardsResponse(
            List<SinglePopularBoard> boards) {

        return PopularBoardsResponse.builder()
                .boards(boards)
                .build();
    }
}
