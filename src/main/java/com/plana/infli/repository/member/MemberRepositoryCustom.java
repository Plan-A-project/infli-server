package com.plana.infli.repository.member;

import com.plana.infli.domain.Member;
import java.util.Optional;

public interface MemberRepositoryCustom {

    Optional<Member> findActiveMemberBy(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findActiveMemberWithUniversityBy(String email);

}
