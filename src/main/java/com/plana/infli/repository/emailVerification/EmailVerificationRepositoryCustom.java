package com.plana.infli.repository.emailVerification;

import com.plana.infli.domain.EmailVerification;
import java.util.Optional;

public interface EmailVerificationRepositoryCustom {


	Optional<EmailVerification> findWithMemberBy(String secret);
}
