package com.plana.infli.web.dto.request.board.popular.edit.service;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EditPopularBoardSequenceServiceRequest {

    private List<Long> popularBoardIds = new ArrayList<>();

    @Builder
    public EditPopularBoardSequenceServiceRequest(List<Long> popularBoardIds) {
        this.popularBoardIds = popularBoardIds;
    }


}
