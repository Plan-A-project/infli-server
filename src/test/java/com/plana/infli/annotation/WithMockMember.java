package com.plana.infli.annotation;

import static com.plana.infli.domain.Role.*;
import static java.lang.annotation.RetentionPolicy.*;

import com.plana.infli.domain.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RUNTIME)
@WithSecurityContext(factory = MockMemberFactory.class)
public @interface WithMockMember {

    String nickname() default "youngjin";

    String username() default "youngjin1234";

    Role role() default STUDENT;

    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_EXECUTION;
}
