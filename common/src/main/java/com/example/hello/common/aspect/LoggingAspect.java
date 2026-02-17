package com.example.hello.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.hello.common..*Service.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("→ Entering: {}", methodName);
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("← Exiting: {} [{}ms]", methodName, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("✖ Exception in: {} [{}ms] - {}", methodName, elapsed, e.getMessage());
            throw e;
        }
    }

    @AfterReturning(pointcut = "execution(* com.example.hello.common..*Service.create*(..))", returning = "result")
    public void auditCreate(Object result) {
        log.info("AUDIT: Created entity → {}", result != null ? result.getClass().getSimpleName() : "null");
    }

    @AfterReturning(pointcut = "execution(* com.example.hello.common..*Service.approve*(..))", returning = "result")
    public void auditApproval(Object result) {
        log.info("AUDIT: Approval action → {}", result != null ? result.getClass().getSimpleName() : "null");
    }
}
