package com.plana.infli.web.dto.response.board.member;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MemberSingleBoard {

    private final Long boardId;

    private final String boardName;


    @QueryProjection
    public MemberSingleBoard(Long boardId, String boardName) {
        this.boardId = boardId;
        this.boardName = boardName;
    }
}
