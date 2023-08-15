package com.plana.infli.repository.emailVerification;

import static com.plana.infli.domain.QEmailVerification.*;
import static com.plana.infli.domain.QMember.*;
import static java.util.Optional.*;

import com.plana.infli.domain.EmailVerification;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;



	@Override
	public Optional<EmailVerification> findWithMemberBy(String code) {
		return ofNullable(jpaQueryFactory.selectFrom(emailVerification)
				.innerJoin(emailVerification.member, member).fetchJoin()
				.where(emailVerification.code.eq(code))
				.fetchOne());
	}
}
