package com.marcomarchionni.ibportfolio.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static com.marcomarchionni.ibportfolio.logging.LoggingUtils.*;

@Aspect
@Component
public class MainLoggingAspect {

    @Before("com.marcomarchionni.ibportfolio.logging.Pointcuts.serviceAndControllerClasses()")
    public void logClassMethodNameAndParameters(JoinPoint joinPoint) {
        if (hasParameters(joinPoint)) {
            logCall(getClassAndMethodName(joinPoint) + " called with param(s) " + getParamNamesAndValues(joinPoint));
        } else {
            logCall(getClassAndMethodName(joinPoint) + " called");
        }
    }
}


