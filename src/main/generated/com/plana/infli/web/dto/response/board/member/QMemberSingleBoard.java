package com.plana.infli.web.dto.response.board.member;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.plana.infli.web.dto.response.board.member.QMemberSingleBoard is a Querydsl Projection type for MemberSingleBoard
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberSingleBoard extends ConstructorExpression<MemberSingleBoard> {

    private static final long serialVersionUID = 1569482729L;

    public QMemberSingleBoard(com.querydsl.core.types.Expression<Long> boardId, com.querydsl.core.types.Expression<String> boardName) {
        super(MemberSingleBoard.class, new Class<?>[]{long.class, String.class}, boardId, boardName);
    }

}

