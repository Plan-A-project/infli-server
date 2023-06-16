package com.plana.infli.web.dto.response.board.all;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.plana.infli.web.dto.response.board.all.QSingleBoard is a Querydsl Projection type for SingleBoard
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QSingleBoard extends ConstructorExpression<SingleBoard> {

    private static final long serialVersionUID = 1736392856L;

    public QSingleBoard(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> boardName) {
        super(SingleBoard.class, new Class<?>[]{long.class, String.class}, id, boardName);
    }

}

