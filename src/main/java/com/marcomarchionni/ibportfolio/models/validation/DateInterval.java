package com.marcomarchionni.ibportfolio.models.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Repeatable(DateIntervals.class)
@Constraint(validatedBy = DateIntervalValidator.class)
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface DateInterval {

        String message() default "dateFrom, dateTo should be within Min and Max date. dateFrom should be equal or before dateTo";
        Class <?> [] groups() default {};
        Class <? extends Payload> [] payload() default {};

        String dateFrom();
        String dateTo();
}
