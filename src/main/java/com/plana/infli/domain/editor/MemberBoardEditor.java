package com.plana.infli.domain.editor;

import com.plana.infli.domain.MemberBoard;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberBoardEditor {

    private final Long order;

    @Builder
    public MemberBoardEditor(Long order) {
        this.order = order;
    }

    public static void editOrder(Long newOrder, MemberBoard memberBoard) {
        memberBoard.edit(memberBoard.toEditor()
                .order(newOrder)
                .build());
    }
}
