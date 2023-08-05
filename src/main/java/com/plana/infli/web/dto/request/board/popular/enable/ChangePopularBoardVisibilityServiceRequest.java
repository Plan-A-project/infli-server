package com.plana.infli.web.dto.request.board.popular.enable;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChangePopularBoardVisibilityServiceRequest {

    List<Long> boardIds;

    @Builder
    public ChangePopularBoardVisibilityServiceRequest(List<Long> boardIds) {
        this.boardIds = boardIds;
    }
}
