package com.plana.infli.repository.email_authentication;

import java.util.Optional;

import com.plana.infli.domain.EmailAuthentication;

public interface EmailAuthenticationRepositoryCustom {

	Optional<EmailAuthentication> findAvailableMemberEmailAuthentication(String secret);
}
