package com.plana.infli.web.dto.response.board.settings.board;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SingleBoard {

    private final Long id;

    private final String boardName;

    @QueryProjection
    public SingleBoard(Long id, String boardName) {
        this.id = id;
        this.boardName = boardName;
    }
}
