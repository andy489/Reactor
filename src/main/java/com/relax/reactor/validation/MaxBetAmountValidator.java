package com.relax.reactor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MaxBetAmountValidator implements ConstraintValidator<MaxBetAmount, Number> {

    private double maxAmount;

    @Override
    public void initialize(MaxBetAmount constraintAnnotation) {
        this.maxAmount = constraintAnnotation.maxAmount();
        
        if (maxAmount <= 0) {
            throw new IllegalArgumentException("maxAmount must be greater than 0");
        }
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        double betAmount = value.doubleValue();

        if (betAmount > maxAmount) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Bet amount $%.2f exceeds maximum allowed amount of $%.2f", betAmount, maxAmount)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}