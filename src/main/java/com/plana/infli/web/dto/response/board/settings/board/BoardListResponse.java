package com.plana.infli.web.dto.response.board.settings.board;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardListResponse {

    private final Long universityId;


    private final List<SingleBoard> boards;

    @Builder
    private BoardListResponse(Long universityId, List<SingleBoard> boards) {
        this.universityId = universityId;
        this.boards = boards != null ? boards : new ArrayList<>();
    }

    public static BoardListResponse createBoardListResponse(Long universityId,
            List<SingleBoard> boards) {

        return BoardListResponse.builder()
                .universityId(universityId)
                .boards(boards).build();
    }

}
