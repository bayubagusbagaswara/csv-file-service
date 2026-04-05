package com.bayu.csvfileservice.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValidationData {

    private final Validator validator;

    public ValidationData(Validator validator) {
        this.validator = validator;
    }

    public <T> Set<ConstraintViolation<T>> validateObject(T target, Class<?>... groups) {
        return validator.validate(target, groups);
    }

}
