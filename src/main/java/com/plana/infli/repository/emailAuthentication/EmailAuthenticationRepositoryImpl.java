package com.plana.infli.repository.emailAuthentication;

import static com.plana.infli.domain.QEmailAuthentication.*;
import static com.plana.infli.domain.QMember.*;
import static java.util.Optional.*;

import com.plana.infli.domain.QEmailAuthentication;
import com.plana.infli.domain.QMember;
import java.util.Optional;

import com.plana.infli.domain.EmailAuthentication;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailAuthenticationRepositoryImpl implements EmailAuthenticationRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<EmailAuthentication> findAvailableEmailAuthentication(String secret) {
//		return Optional.ofNullable(jpaQueryFactory.selectFrom(emailAuthentication)
//			.where(emailAuthentication.secret.eq(secret)
//				.and(emailAuthentication.expirationTime.after(LocalDateTime.now())))
//			.fetchOne());

		return null;

	}

	@Override
	public Optional<EmailAuthentication> findWithMemberBy(String code) {
		return ofNullable(jpaQueryFactory.selectFrom(emailAuthentication)
				.innerJoin(emailAuthentication.member, member).fetchJoin()
				.where(emailAuthentication.code.eq(code))
				.fetchOne());
	}
}
