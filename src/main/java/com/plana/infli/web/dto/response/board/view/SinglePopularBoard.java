package com.plana.infli.web.dto.response.board.view;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SinglePopularBoard {

    // "인기 게시판"에 해당하는 게시판 ID 번호
    // Ex) "인기 동아리글 게시판"이 있다면 동아리 게시판의 ID 번호
    private final Long boardId;

    // 게시판 이름
    private final String boardName;

    // 게시판 종류
    private final String boardType;

    @QueryProjection
    public SinglePopularBoard(Long boardId, String boardName, String boardType) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.boardType = boardType;
    }
}
