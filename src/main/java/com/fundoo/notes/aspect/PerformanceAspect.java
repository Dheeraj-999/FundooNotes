package com.fundoo.notes.aspect;

/*
 * Class Name  : PerformanceAspect
 * Description : Centralized AOP profiling aspect measuring precise execution durations
 *               across service layer calls for bottleneck detection and SLA monitoring.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * Surrounds service method invocations with millisecond-precision timing.
     *
     * @param joinPoint proceeding join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the underlying target method throws an exception
     */
    @Around("execution(* com.fundoo.notes.service..*(..))")
    public Object measureServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimestamp = System.currentTimeMillis();
        String methodSignature = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } finally {
            long executionDurationMillis = System.currentTimeMillis() - startTimestamp;
            logger.info("Service Profiling | Method: {} | Duration: {} ms",
                    methodSignature, executionDurationMillis);
        }
    }
}
