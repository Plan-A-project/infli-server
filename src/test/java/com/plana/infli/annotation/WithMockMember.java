package com.plana.infli.annotation;

import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.springframework.security.test.context.support.TestExecutionEvent.*;

import com.plana.infli.domain.type.Role;
import com.plana.infli.domain.type.VerificationStatus;
import java.lang.annotation.Retention;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RUNTIME)
@WithSecurityContext(factory = MockMemberFactory.class)
public @interface WithMockMember {

    String nickname() default "youngjin";

    String username() default "youngjin1234";

    Role role() default STUDENT;

    boolean policyAccepted() default true;

    VerificationStatus status() default SUCCESS;

    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TEST_EXECUTION;
}
