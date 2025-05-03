package com.example.demo.Domain.Validators.Annotations;

import com.example.demo.Domain.Validators.TimePairValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimePairValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimePair {
    String message() default "Invalid time pair";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
