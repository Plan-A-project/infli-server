package com.plana.infli.repository.company;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {

    Optional<Company> findByName(String companyName);
}
