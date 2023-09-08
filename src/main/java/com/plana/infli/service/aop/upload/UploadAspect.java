package com.plana.infli.service.aop.upload;

import static com.plana.infli.infra.exception.custom.InternalServerErrorException.IMAGE_UPLOAD_FAILED;

import com.plana.infli.infra.exception.custom.InternalServerErrorException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class UploadAspect {


    @Around("@annotation(upload)")
    public Object upload(ProceedingJoinPoint joinPoint, Upload upload) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (IOException e) {
            throw new InternalServerErrorException(IMAGE_UPLOAD_FAILED);
        }
    }
}
