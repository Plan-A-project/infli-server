package com.plana.infli.repository.board;

import static com.plana.infli.domain.QBoard.*;
import static com.plana.infli.domain.QUniversity.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.board.all.QSingleBoard;
import com.plana.infli.web.dto.response.board.all.SingleBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;



    @Override
    public List<SingleBoard> findAllExistingBoards(University findUniversity) {
        return jpaQueryFactory.select(new QSingleBoard(board.id, board.boardName))
                .from(board)
                .innerJoin(board.university, university)
                .where(board.university.eq(findUniversity))
                .orderBy(board.id.asc())
                .fetch();
    }

    @Override
    public Board findByUniversityIDAndBoardName(Long universityId, String boardName) {
        return jpaQueryFactory.selectFrom(board)
                .innerJoin(board.university, university)
                .where(board.university.id.eq(universityId))
                .where(board.boardName.eq(boardName))
                .fetchOne();
    }

    @Override
    public Boolean existsByIdAndUniversity(Long id, University university) {
        Integer fetchFirst = jpaQueryFactory.selectOne()
                .from(board)
                .where(board.id.eq(id))
                .where(board.university.eq(university))
                .fetchFirst();

        return fetchFirst != null;
    }
}
