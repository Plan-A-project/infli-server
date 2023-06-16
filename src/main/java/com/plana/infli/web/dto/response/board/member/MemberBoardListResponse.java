package com.plana.infli.web.dto.response.board.member;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class MemberBoardListResponse {

    private final Integer totalBoards;

    private final List<MemberSingleBoard> boards;

    public MemberBoardListResponse(Integer totalBoards, List<MemberSingleBoard> boards) {
        this.totalBoards = totalBoards;
        this.boards = boards != null ? boards : new ArrayList<>();
    }
}
