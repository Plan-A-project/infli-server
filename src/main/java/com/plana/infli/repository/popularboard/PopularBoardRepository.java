package com.plana.infli.repository.popularboard;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.PopularBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBoardRepository extends JpaRepository<PopularBoard, Long>,
        PopularBoardRepositoryCustom {

    boolean existsByMember(Member member);

}
