package com.example.demo.Domain.Validators;

import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.WeeklySchedule;
import com.example.demo.Domain.Validators.Annotations.ValidSchedule;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class WeeklyScheduleValidator implements ConstraintValidator<ValidSchedule, List<Shift>> {
    @Override

    public boolean isValid(List<Shift> shiftList, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        boolean valid=true;

        try {
           WeeklySchedule.getInstance(shiftList);
        }catch (OperationConditionsFailedException e){
            constraintValidatorContext.buildConstraintViolationWithTemplate("Overlapping timepairs between shifts")
                    .addConstraintViolation();
            valid=false;
        }
        return valid;
    }
}
