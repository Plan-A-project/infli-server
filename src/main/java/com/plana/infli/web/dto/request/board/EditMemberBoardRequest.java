package com.plana.infli.web.dto.request.board;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberBoardRequest {

    private List<Long> memberBoardIds = new ArrayList<>();

    @Builder
    public EditMemberBoardRequest(List<Long> memberBoardIds) {
        this.memberBoardIds = memberBoardIds;
    }
}
