package com.plana.infli.repository.emailAuthentication;

import static com.plana.infli.domain.QMember.*;
import static java.util.Optional.*;

import com.plana.infli.domain.EmailVerification;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailAuthenticationRepositoryImpl implements EmailAuthenticationRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<EmailVerification> findAvailableEmailAuthentication(String secret) {
//		return Optional.ofNullable(jpaQueryFactory.selectFrom(emailAuthentication)
//			.where(emailAuthentication.secret.eq(secret)
//				.and(emailAuthentication.expirationTime.after(LocalDateTime.now())))
//			.fetchOne());

		return null;

	}

	@Override
	public Optional<EmailVerification> findWithMemberBy(String code) {
//		return ofNullable(jpaQueryFactory.selectFrom(emailAuthentication)
//				.innerJoin(emailAuthentication.member, member).fetchJoin()
//				.where(emailAuthentication.code.eq(code))
//				.fetchOne());

		return null;
	}
}
