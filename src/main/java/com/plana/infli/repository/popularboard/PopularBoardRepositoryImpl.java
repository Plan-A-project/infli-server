package com.plana.infli.repository.popularboard;

import static com.plana.infli.domain.QBoard.board;
import static com.plana.infli.domain.QMember.member;
import static com.plana.infli.domain.QPopularBoard.*;
import static java.util.Optional.*;

import com.plana.infli.domain.PopularBoard;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.QPopularBoard;
import com.plana.infli.web.dto.response.board.settings.polularboard.QSinglePopularBoardForSetting;
import com.plana.infli.web.dto.response.board.settings.polularboard.SinglePopularBoardForSetting;
import com.plana.infli.web.dto.response.board.view.QSinglePopularBoard;
import com.plana.infli.web.dto.response.board.view.SinglePopularBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularBoardRepositoryImpl implements PopularBoardRepositoryCustom {


    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<SinglePopularBoardForSetting> findAllEnabledPopularBoardsForSettingBy(Member findMember) {

        //TODO
        //정렬 순서
        return jpaQueryFactory.select(
                        new QSinglePopularBoardForSetting(popularBoard.id, popularBoard.board.boardName,
                                popularBoard.board.boardType.stringValue()))
                .from(popularBoard)
                .innerJoin(popularBoard.board, board)
                .innerJoin(popularBoard.member, member)
                .where(popularBoard.isEnabled.isTrue())
                .where(popularBoard.board.isDeleted.isFalse())
                .where(popularBoard.member.eq(findMember))
                .orderBy(popularBoard.sequence.asc())
                .fetch();
    }

    @Override
    public List<PopularBoard> findAllMemberBoardWithMemberAndBoard() {
        return jpaQueryFactory.selectFrom(popularBoard)
                .innerJoin(popularBoard.member, member).fetchJoin()
                .innerJoin(popularBoard.board, board).fetchJoin()
                .fetch();
    }

    @Override
    public List<PopularBoard> findAllWithBoardOrderByDefaultSequenceBy(Member findMember) {
        return jpaQueryFactory.selectFrom(popularBoard)
                .innerJoin(popularBoard.board, board).fetchJoin()
                .innerJoin(popularBoard.member, member)
                .where(popularBoard.member.eq(findMember))
                .orderBy(popularBoard.sequence.asc())
                .fetch();
    }

    @Override
    public Integer findLatestSequenceNumber() {
        return jpaQueryFactory.select(popularBoard.sequence)
                .from(popularBoard)
                .orderBy(popularBoard.sequence.desc())
                .limit(1)
                .fetchFirst();
    }


    @Override
    public List<SinglePopularBoard> loadEnabledPopularBoardsBy(Member findMember) {
        return jpaQueryFactory.select(new QSinglePopularBoard(
                        popularBoard.board.id, popularBoard.board.boardName,
                        popularBoard.board.boardType.stringValue()))
                .from(popularBoard)
                .innerJoin(popularBoard.board, board)
                .innerJoin(popularBoard.member, member)
                .where(popularBoard.member.eq(findMember))
                .orderBy(popularBoard.sequence.asc())
                .fetch();
    }

    @Override
    public Optional<PopularBoard> findWithMemberById(Long id) {
        return ofNullable(jpaQueryFactory
                .selectFrom(popularBoard)
                .innerJoin(popularBoard.member, member).fetchJoin()
                .where(popularBoard.id.eq(id))
                .fetchOne());
    }

    @Override
    public int findEnabledPopularBoardCountBy(Member findMember) {
        Long count = jpaQueryFactory.select(popularBoard.count())
                .from(popularBoard)
                .innerJoin(popularBoard.member, member)
                .where(popularBoard.isEnabled.isTrue())
                .where(popularBoard.member.eq(findMember))
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

}
