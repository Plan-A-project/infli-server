package com.plana.infli.web.dto.request.board.popular.edit;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditPopularBoardSequenceServiceRequest {

    private final String username;

    private final List<Long> popularBoardIds;

    @Builder
    public EditPopularBoardSequenceServiceRequest(String username, List<Long> popularBoardIds) {
        this.username = username;
        this.popularBoardIds = popularBoardIds;
    }
}
