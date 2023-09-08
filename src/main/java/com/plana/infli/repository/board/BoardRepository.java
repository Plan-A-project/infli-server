package com.plana.infli.repository.board;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {


    boolean existsByBoardTypeAndUniversity(BoardType boardType, University university);
}
