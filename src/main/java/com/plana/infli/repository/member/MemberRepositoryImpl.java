package com.plana.infli.repository.member;

import static com.plana.infli.domain.QMember.*;
import static com.plana.infli.domain.QUniversity.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.util.Optional.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.QMember;
import com.plana.infli.domain.QUniversity;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.web.dto.response.member.verification.student.QStudentVerificationImage;
import com.plana.infli.web.dto.response.member.verification.student.StudentVerificationImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByVerifiedUniversityEmail(String universityEmail) {
        Integer fetchOne = jpaQueryFactory.selectOne()
                .from(member)
                .where(member.studentCredentials.universityEmail.eq(universityEmail))
                .where(member.verificationStatus.eq(SUCCESS))
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer fetchOne = jpaQueryFactory.selectOne()
                .from(member)
                .where(member.loginCredentials.username.eq(username))
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public Optional<Member> findActiveMemberBy(String username) {
        return ofNullable(jpaQueryFactory.selectFrom(member)
                .where(member.loginCredentials.username.eq(username))
                .where(member.basicCredentials.isDeleted.isFalse())
                .fetchOne());
    }

    @Override
    public boolean existsByNickname(String nickname) {
        Integer fetchOne = jpaQueryFactory.selectOne()
                .from(member)
                .where(member.basicCredentials.nickname.eq(nickname))
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public Optional<Member> findActiveMemberWithUniversityBy(String username) {
        return ofNullable(jpaQueryFactory.selectFrom(member)
                .leftJoin(member.university, university).fetchJoin()
                .where(member.loginCredentials.username.eq(username))
                .where(member.basicCredentials.isDeleted.isFalse())
                .fetchOne());
    }

    @Override
    public List<StudentVerificationImage> loadStudentVerificationImages(University university,
            int page) {

        return jpaQueryFactory
                .select(new QStudentVerificationImage(member.id,
                        member.studentCredentials.universityCertificateUrl,
                        member.studentCredentials.realName, member.university.name,
                        member.createdAt))
                .from(member)
                .where(member.university.eq(university))
                .where(member.basicCredentials.isDeleted.isFalse())
                .where(member.role.eq(STUDENT))
                .where(member.verificationStatus.eq(PENDING))
                .offset((page - 1) * 20L)
                .limit(20)
                .fetch();
    }

    @Override
    public Optional<Member> findActiveMemberBy(Long memberId) {
        return ofNullable(jpaQueryFactory.selectFrom(member)
                .where(member.basicCredentials.isDeleted.isFalse())
                .where(member.id.eq(memberId))
                .fetchOne());
    }
}
