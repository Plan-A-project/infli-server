package com.plana.infli.factory;

import static com.plana.infli.domain.Board.*;
import static com.plana.infli.domain.type.BoardType.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardFactory {

    @Autowired
    private BoardRepository boardRepository;

    public Board createAnonymousBoard(University university) {
        return boardRepository.save(create(ANONYMOUS, university, ANONYMOUS.getBoardName()));
    }

    public Board createClubBoard(University university) {
        return boardRepository.save(create(CLUB, university, CLUB.getBoardName()));
    }

    public Board createActivityBoard(University university) {
        return boardRepository.save(create(ACTIVITY, university, ACTIVITY.getBoardName()));
    }

    public Board createEmploymentBoard(University university) {
        return boardRepository.save(create(EMPLOYMENT, university, EMPLOYMENT.getBoardName()));
    }

    public Board createCampusLifeBoard(University university) {
        return boardRepository.save(create(CAMPUS_LIFE, university, CAMPUS_LIFE.getBoardName()));
    }

    public Board createByBoardType(University university, BoardType boardType) {
        return boardRepository.save(create(boardType, university, ""));
    }

}
