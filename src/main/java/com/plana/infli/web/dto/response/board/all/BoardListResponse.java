package com.plana.infli.web.dto.response.board.all;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class BoardListResponse {

    private final Integer boardCount;

    private final List<SingleBoard> boards;

    public BoardListResponse(Integer boardCount, List<SingleBoard> boards) {
        this.boardCount = boardCount;
        this.boards = boards != null ? boards : new ArrayList<>();
    }
}
