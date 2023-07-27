package com.plana.infli.config.initializer;

import static com.plana.infli.domain.Board.create;
import static com.plana.infli.domain.BoardType.*;

import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile({"dev", "prod", "local"})
public class DefaultInitializer implements CommandLineRunner {

    private static final String FUDAN = "푸단대학교";

    private final BoardRepository boardRepository;

    private final UniversityRepository universityRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        University university = createDefaultUniversityIfNotExists();

        createDefaultBoardsIfNotExists(university);
    }


    private University createDefaultUniversityIfNotExists() {

        return universityRepository.findByName(FUDAN)
                .orElseGet(() -> universityRepository.save(University.create(FUDAN)));
    }

    private void createDefaultBoardsIfNotExists(University university) {
        createEmploymentBoardIfNull(university);

        createActivityBoardIfNull(university);

        createClubBoardIfNull(university);

        createAnonymousBoardIfNull(university);

        createCampusLifeBoardIfNull(university);
    }

    private void createEmploymentBoardIfNull(University university) {
        if (boardRepository.existsByBoardTypeAndUniversity(EMPLOYMENT, university) == false) {
            boardRepository.save(create(EMPLOYMENT, university));

        }
    }

    private void createActivityBoardIfNull(University university) {
        if (boardRepository.existsByBoardTypeAndUniversity(ACTIVITY, university) == false) {
            boardRepository.save(create(ACTIVITY, university));
        }
    }

    private void createClubBoardIfNull(University university) {
        if (boardRepository.existsByBoardTypeAndUniversity(CLUB, university) == false) {
            boardRepository.save(create(CLUB, university));
        }
    }


    private void createAnonymousBoardIfNull(University university) {
        if (boardRepository.existsByBoardTypeAndUniversity(ANONYMOUS, university) == false) {
            boardRepository.save(create(ANONYMOUS, university));
        }
    }

    private void createCampusLifeBoardIfNull(University university) {
        if (boardRepository.existsByBoardTypeAndUniversity(CAMPUS_LIFE, university) == false) {
            boardRepository.save(create(CAMPUS_LIFE, university));
        }
    }
}
