package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.editor.popularboard.PopularBoardEditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
// 게시판에 존재하는 인기 글을 모아놓은 게시판
// Ex) 동아리 게시판에 존재하는 인기글을 모아놓은 "인기 동아리 게시판"
public class PopularBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_board_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private int sequence;

    private boolean isEnabled = true;

    @Builder
    public PopularBoard(Member member, Board board, int sequence) {
        this.member = member;
        this.board = board;
        this.sequence = sequence;
    }


    public static PopularBoard newPopularBoard(Member member, Board board) {
        return PopularBoard.builder()
                .member(member)
                .board(board)
                .sequence(board.getBoardType().getDefaultSequence())
                .build();
    }

    public void editSequence(PopularBoardEditor popularBoardEditor) {
        this.sequence = popularBoardEditor.getSequence() != null ?
                popularBoardEditor.getSequence() : sequence;

        this.isEnabled = popularBoardEditor.getIsEnabled() != null ?
                popularBoardEditor.getIsEnabled() : isEnabled;
    }

    public void editVisibility(PopularBoardEditor popularBoardEditor) {
        this.isEnabled = popularBoardEditor.getIsEnabled() != null ?
                popularBoardEditor.getIsEnabled() : isEnabled;
    }

    public PopularBoardEditor.PopularBoardEditorBuilder toEditor() {
        return PopularBoardEditor.builder()
                .sequence(sequence)
                .isEnabled(isEnabled);
    }
}
