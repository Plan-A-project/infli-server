package com.plana.infli.repository.board;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.board.all.SingleBoard;
import java.util.List;

public interface BoardRepositoryCustom {


    List<SingleBoard> findAllExistingBoards(University university);

    Board findByUniversityIDAndBoardName(Long universityId, String boardName);

    Boolean existsByIdAndUniversity(Long id, University university);

}
