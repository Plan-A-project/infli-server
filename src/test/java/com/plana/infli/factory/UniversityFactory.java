package com.plana.infli.factory;

import com.plana.infli.domain.University;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniversityFactory {

    private final UniversityRepository universityRepository;

    public University createUniversity(String name) {
        return universityRepository.save(University.builder()
                .name(name).build());
    }
}
