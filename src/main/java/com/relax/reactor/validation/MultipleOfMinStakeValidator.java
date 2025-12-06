package com.relax.reactor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MultipleOfMinStakeValidator implements ConstraintValidator<MultipleOfMinStake, Number> {

    private double minStake;
    private boolean allowZero;
    private boolean allowNegative;
    private double tolerance;

    @Override
    public void initialize(MultipleOfMinStake constraintAnnotation) {
        this.minStake = constraintAnnotation.minStake();
        this.allowZero = constraintAnnotation.allowZero();
        this.allowNegative = constraintAnnotation.allowNegative();
        this.tolerance = constraintAnnotation.tolerance();

        if (minStake <= 0) {
            throw new IllegalArgumentException("minStake must be greater than 0");
        }
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // We are using @NotNull, so null is valid
        }

        double stakeValue = value.doubleValue();

        if (!allowZero && stakeValue == 0) {
            addCustomMessage(context, "Stake cannot be zero");
            return false;
        }

        if (!allowNegative && stakeValue < 0) {
            addCustomMessage(context, "Stake cannot be negative");
            return false;
        }

        if (!isMultipleOf(stakeValue)) {

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format("Stake %.2f must be a multiple of %.2f",
                            stakeValue, minStake)).addConstraintViolation();

            return false;
        }

        return true;
    }

    private boolean isMultipleOf(double value) {
        BigDecimal stake = BigDecimal.valueOf(value);
        BigDecimal multiple = BigDecimal.valueOf(minStake);

        BigDecimal remainder = stake.remainder(multiple);

        BigDecimal absRemainder = remainder.abs();
        BigDecimal toleranceBD = BigDecimal.valueOf(tolerance);

        return absRemainder.compareTo(toleranceBD) <= 0 ||
                absRemainder.subtract(multiple).abs().compareTo(toleranceBD) <= 0;
    }

    private void addCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
