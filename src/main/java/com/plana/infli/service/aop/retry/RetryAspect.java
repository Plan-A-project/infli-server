package com.plana.infli.service.aop.retry;

import static com.plana.infli.infra.exception.custom.InternalServerErrorException.POST_VIEW_FAILED;

import com.plana.infli.infra.exception.custom.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RetryAspect {

    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {

        int maxRetry = retry.value();

        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {
                return joinPoint.proceed();
            } catch (ObjectOptimisticLockingFailureException e) {
                try {
                    log.info("[retry] try count={}/{}", retryCount, maxRetry);
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new InternalServerErrorException(POST_VIEW_FAILED);
    }
}
