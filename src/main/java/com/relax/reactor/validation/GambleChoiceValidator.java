package com.relax.reactor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GambleChoiceValidator implements ConstraintValidator<ValidGambleChoice, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        
        return value == 1 || value == 2;
    }
}
