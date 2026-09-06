package com.ecommerce.product.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProductLoggingAspect {
    private static final Logger log =LoggerFactory.getLogger(ProductLoggingAspect.class);

    @Around("@annotation(com.ecommerce.product.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint)
            throws Throwable {

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long executionTime =
                System.currentTimeMillis() - startTime;

        log.info(
                "Method: {} executed in {} ms",
                joinPoint.getSignature().getName(),
                executionTime
        );

        return result;
    }
}