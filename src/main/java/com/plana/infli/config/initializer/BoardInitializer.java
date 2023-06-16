package com.plana.infli.config.initializer;

import com.plana.infli.domain.Board;
import com.plana.infli.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardInitializer implements CommandLineRunner {

    private static final String Employment = "취업";

    private static final String ACTIVITY = "대외활동";
    
    private static final String ANONYMOUS = "익명";

    private static final String CLUB = "동아리";
    

    private final BoardRepository boardRepository;


    @Override
    public void run(String... args) throws Exception {

        createEmployment();

        createActivity();

        createClub();

//        createAnonymous();
    }

//    private void createAnonymous() {
//        if (boardRepository.existsByBoardName(ANONYMOUS)) {
//            return
//        }
//
//        Board board = Board
//    }

    private void createActivity() {
    }

    private void createClub() {
    }

    private void createEmployment() {
        
    }
}
