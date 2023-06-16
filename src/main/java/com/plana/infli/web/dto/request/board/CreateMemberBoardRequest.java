package com.plana.infli.web.dto.request.board;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMemberBoardRequest {

    List<Long> ids = new ArrayList<>();

    @Builder
    public CreateMemberBoardRequest(List<Long> ids) {
        this.ids = ids;
    }
}
