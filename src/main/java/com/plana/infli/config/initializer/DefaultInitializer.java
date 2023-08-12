package com.plana.infli.config.initializer;

import static com.plana.infli.domain.Board.create;
import static com.plana.infli.domain.type.BoardType.*;

import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile({"dev", "prod", "local"})
@Order(0)
public class DefaultInitializer implements CommandLineRunner {

    private static final String FUDAN = "푸단대학교";

    private final BoardRepository boardRepository;

    private final UniversityRepository universityRepository;

    private University university;

    @Override
    @Transactional
    public void run(String... args) {

        university = universityRepository.findByName(FUDAN)
                .orElseGet(() -> universityRepository.save(University.create(FUDAN)));


        createBoardWithType(EMPLOYMENT);
        createBoardWithType(ACTIVITY);
        createBoardWithType(CLUB);
        createBoardWithType(ANONYMOUS);
        createBoardWithType(CAMPUS_LIFE);
    }


    private void createBoardWithType(BoardType boardType) {
        if (boardRepository.existsByBoardTypeAndUniversity(boardType, university) == false) {
            boardRepository.save(create(boardType, university));
        }
    }
}
