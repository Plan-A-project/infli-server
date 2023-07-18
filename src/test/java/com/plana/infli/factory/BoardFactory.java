package com.plana.infli.factory;

import static com.plana.infli.domain.BoardType.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.BoardType;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardFactory {

    @Autowired
    private BoardRepository boardRepository;

    public Board createAnonymousBoard(University university) {
        return boardRepository.save(Board.create(ANONYMOUS, university));
    }

    public Board createClubBoard(University university) {
        return boardRepository.save(Board.create(CLUB, university));
    }

    public Board createActivityBoard(University university) {
        return boardRepository.save(Board.create(ACTIVITY, university));
    }

    public Board createEmploymentBoard(University university) {
        return boardRepository.save(Board.create(EMPLOYMENT, university));
    }

    public Board createCampusLifeBoard(University university) {
        return boardRepository.save(Board.create(CAMPUS_LIFE, university));
    }


}
