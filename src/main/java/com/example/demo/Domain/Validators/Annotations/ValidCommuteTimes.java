package com.example.demo.Domain.Validators.Annotations;

import com.example.demo.Domain.Validators.CommuteTimeValidator;
import com.example.demo.Domain.Validators.WorkExperienceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CommuteTimeValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCommuteTimes {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
