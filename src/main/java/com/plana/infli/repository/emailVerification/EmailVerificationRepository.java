package com.plana.infli.repository.emailVerification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.EmailVerification;

public interface EmailVerificationRepository
	extends JpaRepository<EmailVerification, Long>, EmailVerificationRepositoryCustom {

	Optional<EmailVerification> findByCode(String code);
}
