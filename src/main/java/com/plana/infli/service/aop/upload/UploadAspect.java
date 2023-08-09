package com.plana.infli.service.aop.upload;

import static com.plana.infli.exception.custom.InternalServerErrorException.IMAGE_UPLOAD_FAILED;
import static com.plana.infli.exception.custom.InternalServerErrorException.POST_VIEW_FAILED;

import com.plana.infli.exception.custom.InternalServerErrorException;
import com.plana.infli.service.aop.retry.Retry;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UploadAspect {


    @Around("@annotation(upload)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Upload upload) throws Throwable {

        int maxRetry = upload.value();

        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {
                return joinPoint.proceed();
            } catch (IOException e) {
                try {
                    log.info("[retry] try count={}/{}", retryCount, maxRetry);
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new InternalServerErrorException(IMAGE_UPLOAD_FAILED);
    }
}
