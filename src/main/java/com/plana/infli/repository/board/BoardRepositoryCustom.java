package com.plana.infli.repository.board;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.board.settings.board.SingleBoard;
import java.util.List;
import java.util.Optional;

public interface BoardRepositoryCustom {


    List<SingleBoard> loadAllBoardBy(University university);

    Optional<Board> findActiveBoardBy(Long boardId);

    List<Board> findAllActiveBoardBy(University university);

    List<Board> findAllWithUniversityByIdIn(List<Long> ids);

    Optional<Board> findActiveBoardWithUniversityBy(Long boardId);

}
