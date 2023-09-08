package com.plana.infli.factory;

import com.plana.infli.domain.University;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniversityFactory {

    @Autowired
    private UniversityRepository universityRepository;

    public University createUniversity(String name) {
        return universityRepository.save(University.builder()
                .name(name).build());
    }
}
