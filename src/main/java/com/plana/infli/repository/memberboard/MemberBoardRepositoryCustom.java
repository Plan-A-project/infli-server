package com.plana.infli.repository.memberboard;

import com.plana.infli.domain.Member;
import com.plana.infli.web.dto.response.board.member.MemberSingleBoard;
import java.util.List;

public interface MemberBoardRepositoryCustom {


    void bulkDeleteExistingMemberBoard(Member member);

    List<MemberSingleBoard> findMemberBoards(Member member);

    Boolean existsByIdAndMember(Long id, Member member);
}
