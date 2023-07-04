package com.plana.infli.web.dto.response.board.settings.polularboard;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SinglePopularBoardForSetting {

    private final Long memberBoardId;

    private final String boardName;

    private final String boardType;


    @QueryProjection
    public SinglePopularBoardForSetting(Long memberBoardId, String boardName, String boardType) {
        this.memberBoardId = memberBoardId;
        this.boardName = boardName;
        this.boardType = boardType;
    }
}
