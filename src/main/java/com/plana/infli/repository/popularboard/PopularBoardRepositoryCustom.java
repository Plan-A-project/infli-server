package com.plana.infli.repository.popularboard;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.PopularBoard;
import com.plana.infli.domain.Member;
import com.plana.infli.web.dto.response.board.settings.polularboard.SinglePopularBoardForSetting;
import com.plana.infli.web.dto.response.board.view.SinglePopularBoard;
import java.util.List;
import java.util.Optional;

public interface PopularBoardRepositoryCustom {


    List<SinglePopularBoardForSetting> findAllEnabledPopularBoardsForSettingBy(Member member);

    // 테스트 케이스용 메서드
    List<PopularBoard> findAllMemberBoardWithMemberAndBoard();

    List<PopularBoard> findAllWithBoardOrderBySequenceByMember(Member member);

    Integer findLatestSequenceNumber();

    List<SinglePopularBoard> loadEnabledPopularBoardsBy(Member member);

    Optional<PopularBoard> findWithMemberById(Long id);

    int findEnabledPopularBoardCountBy(Member member);

    // 테스트 케이스용 메서드
    PopularBoard findByBoardAndMember(Board board, Member member);

}
