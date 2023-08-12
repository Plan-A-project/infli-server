package com.plana.infli.repository.emailAuthentication;

import java.util.Optional;

import com.plana.infli.domain.EmailAuthentication;

public interface EmailAuthenticationRepositoryCustom {

	Optional<EmailAuthentication> findAvailableEmailAuthentication(String secret);

	Optional<EmailAuthentication> findWithMemberBy(String secret);
}
