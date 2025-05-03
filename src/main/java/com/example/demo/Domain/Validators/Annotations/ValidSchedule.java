package com.example.demo.Domain.Validators.Annotations;

import com.example.demo.Domain.Validators.TimePairArrayValidator;
import com.example.demo.Domain.Validators.WeeklyScheduleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WeeklyScheduleValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD,ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSchedule {
    String message() default "Invalid schedule";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
