package com.plana.infli.repository.university;

import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.domain.QPost.*;
import static com.plana.infli.domain.QUniversity.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.QMember;
import com.plana.infli.domain.QPost;
import com.plana.infli.domain.QUniversity;
import com.plana.infli.domain.University;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Objects;
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


    @Override
    public Boolean checkIfMemberAndPostIsInSameUniversity(Member findMember, Post findPost) {
        Long memberUniversityId = jpaQueryFactory.select(member.university.id).
                from(member)
                .where(member.eq(findMember))
                .fetchOne();

        Long postUniversityId = jpaQueryFactory.select(post.board.university.id)
                .from(post)
                .where(post.eq(findPost))
                .fetchOne();

        return Objects.equals(memberUniversityId, postUniversityId);
    }
}
