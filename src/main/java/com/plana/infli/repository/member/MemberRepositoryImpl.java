package com.plana.infli.repository.member;

import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.domain.QUniversity.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.QMember;
import com.plana.infli.domain.QUniversity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Member> findActiveMemberBy(String username) {
        return ofNullable(jpaQueryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .where(member.status.isDeleted.isFalse())
                .fetchOne());
    }

    @Override
    public boolean existsByNickname(String nickname) {
        Integer fetchOne = jpaQueryFactory.selectOne()
                .from(member)
                .where(member.name.nickname.eq(nickname))
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public Optional<Member> findActiveMemberWithUniversityBy(String username) {
        return ofNullable(jpaQueryFactory.selectFrom(member)
                .leftJoin(member.university, university).fetchJoin()
                .where(member.username.eq(username))
                .where(member.status.isDeleted.isFalse())
                .fetchOne());
    }
}
