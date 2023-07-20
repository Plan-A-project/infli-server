package com.plana.infli.repository.board;

import static com.plana.infli.domain.QBoard.board;
import static com.plana.infli.domain.QUniversity.university;
import static java.util.Optional.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.board.settings.board.QSingleBoard;
import com.plana.infli.web.dto.response.board.settings.board.SingleBoard;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<SingleBoard> loadAllBoardBy(University findUniversity) {
        return jpaQueryFactory.select(new QSingleBoard(board.id, board.boardName))
                .from(board)
                .innerJoin(board.university, university)
                .where(board.university.eq(findUniversity))
                .where(boardIsNotDeleted())
                .orderBy(board.sequence.asc())
                .fetch();
    }

    @Override
    public Optional<Board> findActiveBoardBy(Long boardId) {
        return ofNullable(jpaQueryFactory.selectFrom(board)
                .where(board.id.eq(boardId))
                .where(boardIsNotDeleted())
                .fetchOne());
    }

    private static BooleanExpression boardIsNotDeleted() {
        return board.isDeleted.isFalse();
    }

    @Override
    public List<Board> findAllActiveBoardBy(University university) {
        return jpaQueryFactory.selectFrom(board)
                .where(boardIsNotDeleted())
                .where(board.university.eq(university))
                .orderBy(board.sequence.asc())
                .fetch();
    }

    @Override
    public List<Board> findAllWithUniversityByIdIn(List<Long> ids) {
        return jpaQueryFactory.selectFrom(board)
                .where(board.id.in(ids))
                .where(boardIsNotDeleted())
                .innerJoin(board.university, university).fetchJoin()
                .fetch();
    }

    @Override
    public Optional<Board> findActiveBoardWithUniversityBy(Long boardId) {
        return ofNullable(jpaQueryFactory.selectFrom(board)
                .where(boardIsNotDeleted())
                .where(board.id.eq(boardId))
                .leftJoin(board.university, university).fetchJoin()
                .fetchOne());
    }
}
