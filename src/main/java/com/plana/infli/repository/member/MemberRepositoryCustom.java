package com.plana.infli.repository.member;

import com.plana.infli.domain.Member;
import java.util.Optional;

public interface MemberRepositoryCustom {

    boolean existsByUniversityEmail(String universityEmail);

    boolean existsByUsername(String username);

    Optional<Member> findActiveMemberBy(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findActiveMemberWithUniversityBy(String username);

}
