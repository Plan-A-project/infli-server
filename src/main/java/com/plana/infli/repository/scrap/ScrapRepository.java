package com.plana.infli.repository.scrap;

import com.plana.infli.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {

}
