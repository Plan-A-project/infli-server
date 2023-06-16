package com.plana.infli.repository.member;

import com.plana.infli.domain.Member;

public interface MemberRepositoryCustom {

    Member findActiveMemberByNickname(String nickname);
}
