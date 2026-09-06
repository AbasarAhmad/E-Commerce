package com.ecommerce.product.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
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
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Method: {} executed in {} ms",joinPoint.getSignature().getName(),executionTime);
        }
    }
    
    
    @AfterThrowing(pointcut = "execution(* com.ecommerce.product.service.*.*(..))",throwing = "exception")
    public void logException(JoinPoint joinPoint,Exception exception) {

        log.error("Exception in method: {} - {}",joinPoint.getSignature().getName(),exception.getMessage());
    }
}