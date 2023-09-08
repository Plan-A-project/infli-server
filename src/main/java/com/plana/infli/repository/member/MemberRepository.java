package com.plana.infli.repository.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

}
