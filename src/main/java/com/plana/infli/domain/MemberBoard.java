package com.plana.infli.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.editor.MemberBoardEditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class MemberBoard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_board_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private Board board;

    private Long order;

    @Builder
    public MemberBoard(Member member, Board board, Long order) {
        this.member = member;
        this.board = board;
        this.order = order;
    }


    public static MemberBoard createNewMemberBoard(Member member, Board board) {
        return MemberBoard.builder()
                .member(member)
                .board(board)
                .order(board.getId())
                .build();
    }

    public void edit(MemberBoardEditor memberBoardEditor) {
        this.order = memberBoardEditor.getOrder();
    }

    public MemberBoardEditor.MemberBoardEditorBuilder toEditor() {
        return MemberBoardEditor.builder()
                .order(this.order);
    }
}
