package com.plana.infli.repository.emailAuthentication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plana.infli.domain.EmailAuthentication;

public interface EmailAuthenticationRepository
	extends JpaRepository<EmailAuthentication, Long>, EmailAuthenticationRepositoryCustom {

	Optional<EmailAuthentication> findByCode(String code);
}
