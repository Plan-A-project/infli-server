package com.plana.infli.repository.email_authentication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.EmailAuthentication;

public interface EmailAuthenticationRepository
	extends JpaRepository<EmailAuthentication, Long>, EmailAuthenticationRepositoryCustom {

	Optional<EmailAuthentication> findBySecret(String secret);
}
