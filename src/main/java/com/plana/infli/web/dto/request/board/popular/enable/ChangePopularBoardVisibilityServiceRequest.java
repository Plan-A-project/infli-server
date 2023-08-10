package com.plana.infli.web.dto.request.board.popular.enable;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChangePopularBoardVisibilityServiceRequest {

    private final String username;

    private final List<Long> boardIds;

    @Builder
    public ChangePopularBoardVisibilityServiceRequest(String username, List<Long> boardIds) {
        this.username = username;
        this.boardIds = boardIds;
    }
}
