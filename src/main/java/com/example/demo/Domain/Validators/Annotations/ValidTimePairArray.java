package com.example.demo.Domain.Validators.Annotations;

import com.example.demo.Domain.Validators.TimePairArrayValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimePairArrayValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimePairArray {
    String message() default "Timepair array is not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
