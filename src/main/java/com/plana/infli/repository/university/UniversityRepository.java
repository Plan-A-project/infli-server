package com.plana.infli.repository.university;

import com.plana.infli.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long>, UniversityRepositoryCustom {


}
