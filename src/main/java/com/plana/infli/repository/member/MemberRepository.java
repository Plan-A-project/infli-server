package com.plana.infli.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}
