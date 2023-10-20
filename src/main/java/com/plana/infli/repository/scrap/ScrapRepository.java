package com.plana.infli.repository.scrap;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.Scrap;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {


    boolean existsByPostAndMember(Post post, Member member);

    Optional<Scrap> findByPostAndMember(Post post, Member member);
}
