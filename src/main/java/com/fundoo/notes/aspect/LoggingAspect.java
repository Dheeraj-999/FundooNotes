package com.fundoo.notes.aspect;

/*
 * Class Name  : LoggingAspect
 * Description : Centralized AOP logging aspect that intercepts service layer
 *               method invocations for structured diagnostic tracing and auditing.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Intercepts execution before entering any service method to log method entry.
     *
     * @param joinPoint metadata about the intercepted execution point
     */
    @Before("execution(* com.fundoo.notes.service..*(..))")
    public void logBeforeServiceMethod(JoinPoint joinPoint) {
        String methodSignature = joinPoint.getSignature().toShortString();
        logger.info("Entering service execution: {}", methodSignature);
    }

    /**
     * Intercepts execution after completing any service method to log method exit.
     *
     * @param joinPoint metadata about the intercepted execution point
     */
    @After("execution(* com.fundoo.notes.service..*(..))")
    public void logAfterServiceMethod(JoinPoint joinPoint) {
        String methodSignature = joinPoint.getSignature().toShortString();
        logger.info("Completed service execution: {}", methodSignature);
    }
}
