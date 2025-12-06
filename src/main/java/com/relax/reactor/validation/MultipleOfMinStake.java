package com.relax.reactor.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import static com.relax.reactor.config.UtilConstants.EPSILON;

@Documented
@Constraint(validatedBy = MultipleOfMinStakeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipleOfMinStake {

    String message() default "Stake must be a multiple of {minStake}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    double minStake() default 0.10;

    boolean allowZero() default false;
    boolean allowNegative() default false;
    double tolerance() default EPSILON;
}