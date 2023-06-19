package com.plana.infli.repository.memberboard;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.MemberBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBoardRepository extends JpaRepository<MemberBoard, Long>, MemberBoardRepositoryCustom{


    MemberBoard findMemberBoardById(Long id);
}
