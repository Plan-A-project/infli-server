package com.plana.infli.config.initializer;

import com.plana.infli.domain.University;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniversityInitializer implements CommandLineRunner {

    private static final String FUDAN = "푸단대학교";

    private final UniversityRepository universityRepository;

    @Override
    public void run(String... args) throws Exception {
        if (universityRepository.existsByUniversityName(FUDAN)) {
            return;
        }

        University university = University
                .builder()
                .universityName(FUDAN)
                .build();

        universityRepository.save(university);
    }
}
