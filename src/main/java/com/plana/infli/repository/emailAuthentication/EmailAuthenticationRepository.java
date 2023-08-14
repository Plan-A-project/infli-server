package com.plana.infli.repository.emailAuthentication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.EmailVerification;

public interface EmailAuthenticationRepository
	extends JpaRepository<EmailVerification, Long>, EmailAuthenticationRepositoryCustom {

	Optional<EmailVerification> findByCode(String code);
}
