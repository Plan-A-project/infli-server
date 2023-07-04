package com.plana.infli.repository.university;

import com.plana.infli.domain.University;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long>, UniversityRepositoryCustom {


    boolean existsByName(String universityName);

    Optional<University> findUniversityById(Long id);

    Optional<University> findByName(String name);
}
