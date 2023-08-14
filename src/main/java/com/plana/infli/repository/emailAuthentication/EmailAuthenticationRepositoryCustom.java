package com.plana.infli.repository.emailAuthentication;

import com.plana.infli.domain.EmailVerification;
import java.util.Optional;

public interface EmailAuthenticationRepositoryCustom {

	Optional<EmailVerification> findAvailableEmailAuthentication(String secret);

	Optional<EmailVerification> findWithMemberBy(String secret);
}
