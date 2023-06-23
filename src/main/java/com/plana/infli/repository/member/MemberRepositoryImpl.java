package com.plana.infli.repository.member;

import static com.plana.infli.domain.QMember.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Member findActiveMemberByEmail(String email) {
        return jpaQueryFactory.selectFrom(member)
                .where(member.isEnabled.isTrue())
                .where(member.email.eq(email))
                .fetchOne();
    }

}
