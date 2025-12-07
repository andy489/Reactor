package com.relax.reactor.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxBetAmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBetAmount {

    String message() default "Bet amount cannot exceed {maxAmount}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    double maxAmount() default 100.0;
}