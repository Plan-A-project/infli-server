package com.plana.infli.repository.memberboard;

import static com.plana.infli.domain.QBoard.board;
import static com.plana.infli.domain.QMember.member;
import static com.plana.infli.domain.QMemberBoard.memberBoard;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.board.member.MemberSingleBoard;
import com.plana.infli.web.dto.response.board.member.QMemberSingleBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberBoardRepositoryImpl implements MemberBoardRepositoryCustom {


    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberSingleBoard> findMemberBoards(Member findMember) {

        List<MemberSingleBoard> boards;

        if (memberBoardExists(findMember)) {
            boards = loadMemberBoards(findMember);
        } else {
            boards = loadBoards(findMember);
        }

        return boards;
    }

    @Override
    public Boolean existsByIdAndMember(Long id, Member member) {
        return jpaQueryFactory.selectOne()
                .from(memberBoard)
                .where(memberBoard.id.eq(id))
                .where(memberBoard.member.eq(member))
                .fetchFirst() != null;
    }

    private boolean memberBoardExists(Member findMember) {
        return jpaQueryFactory.selectOne()
                .from(memberBoard)
                .where(memberBoard.member.eq(findMember))
                .fetchFirst() != null;
    }

    private List<MemberSingleBoard> loadMemberBoards(Member findMember) {
        return jpaQueryFactory.select(
                        new QMemberSingleBoard(memberBoard.board.id, memberBoard.board.boardName))
                .from(memberBoard)
                .innerJoin(memberBoard.board, board)
                .innerJoin(memberBoard.member, member)
                .where(memberBoard.member.eq(findMember))
                .orderBy(memberBoard.order.asc())
                .fetch();
    }


    private List<MemberSingleBoard> loadBoards(Member findMember) {

        University university = findUniversityByMember(findMember);

        return jpaQueryFactory.select(
                        new QMemberSingleBoard(board.id, board.boardName))
                .from(board)
                .where(board.university.eq(university))
                .orderBy(board.id.asc())
                .fetch();
    }

    private University findUniversityByMember(Member findMember) {
        return jpaQueryFactory.select(member.university)
                .from(member)
                .where(member.eq(findMember))
                .fetchOne();
    }

    @Override
    public void bulkDeleteExistingMemberBoard(Member member) {
        jpaQueryFactory.delete(memberBoard)
                .where(memberBoard.member.eq(member))
                .execute();
    }
}
