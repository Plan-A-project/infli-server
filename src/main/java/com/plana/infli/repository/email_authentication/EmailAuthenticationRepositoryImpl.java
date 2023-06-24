package com.plana.infli.repository.email_authentication;

import static com.plana.infli.domain.QEmailAuthentication.*;

import java.time.LocalDateTime;
import java.util.Optional;

import com.plana.infli.domain.EmailAuthentication;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailAuthenticationRepositoryImpl implements EmailAuthenticationRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<EmailAuthentication> findAvailableEmailAuthentication(String secret) {
		return Optional.ofNullable(jpaQueryFactory.selectFrom(emailAuthentication)
			.where(emailAuthentication.secret.eq(secret)
				.and(emailAuthentication.expirationTime.after(LocalDateTime.now())))
			.fetchOne());

	}
}
