package com.plana.infli.web.dto.request.board.popular.edit.controller;

import com.plana.infli.web.dto.request.board.popular.edit.service.EditPopularBoardSequenceServiceRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditPopularBoardSequenceRequest {

    //TODO
    @NotEmpty(message = "인기 게시판이 선택되지 않았습니다")
    private List<Long> popularBoardIds = new ArrayList<>();

    @Builder
    public EditPopularBoardSequenceRequest(List<Long> popularBoardIds) {
        this.popularBoardIds = popularBoardIds;
    }

    public EditPopularBoardSequenceServiceRequest toServiceRequest() {
        return EditPopularBoardSequenceServiceRequest.builder()
                .popularBoardIds(popularBoardIds)
                .build();
    }
}
