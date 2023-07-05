package com.plana.infli.factory;

import static com.plana.infli.domain.BoardType.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardFactory {

    private final BoardRepository boardRepository;

    public Board createAnonymousBoard(University university) {
        return boardRepository.save(Board.create(ANONYMOUS, university));
    }

    public Board createNonAnonymousBoard(University university) {
        return boardRepository.save(Board.create(CLUB, university));
    }
}
