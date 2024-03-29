package com.plana.infli.web.dto.request.board.popular.enable;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePopularBoardVisibilityRequest {

    @NotEmpty(message = "보고싶은 게시판을 한개 이상 선택해주세요")
    List<Long> boardIds = new ArrayList<>();

    @Builder
    public ChangePopularBoardVisibilityRequest(List<Long> boardIds) {
        this.boardIds = boardIds;
    }

    public ChangePopularBoardVisibilityServiceRequest toServiceRequest(String username) {
        return ChangePopularBoardVisibilityServiceRequest.builder()
                .username(username)
                .boardIds(boardIds)
                .build();
    }
}
