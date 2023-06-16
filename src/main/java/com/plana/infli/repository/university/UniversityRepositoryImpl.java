package com.plana.infli.repository.university;

import static com.plana.infli.domain.QMember.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniversityRepositoryImpl implements UniversityRepositoryCustom {


    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public University findByMember(Member findMember) {
        return jpaQueryFactory.select(member.university)
                .from(member)
                .where(member.eq(findMember))
                .fetchOne();
    }
}
