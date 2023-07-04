package com.plana.infli.domain.editor.popularboard;

import com.plana.infli.domain.PopularBoard;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PopularBoardEditor {

    private final Integer sequence;

    private final Boolean isEnabled;

    @Builder
    public PopularBoardEditor(Integer sequence, Boolean isEnabled) {
        this.sequence = sequence;
        this.isEnabled = isEnabled;
    }

    public static void editSequence(PopularBoard popularBoard, Integer newSequence) {
        popularBoard.editSequence(popularBoard.toEditor()
                .sequence(newSequence)
                .build());
    }

    public static void enableThis(PopularBoard popularBoard) {
        popularBoard.editVisibility(popularBoard.toEditor()
                .isEnabled(true)
                .build());
    }

    public static void disableThis(PopularBoard popularBoard) {
        popularBoard.editVisibility(popularBoard.toEditor()
                .isEnabled(false)
                .build());
    }
}
