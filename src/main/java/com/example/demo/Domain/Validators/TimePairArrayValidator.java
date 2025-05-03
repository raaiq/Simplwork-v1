package com.example.demo.Domain.Validators;

import com.example.demo.Domain.TypesAndEnums.DailySchedule;
import com.example.demo.Domain.TypesAndEnums.TimePair;
import com.example.demo.Domain.Validators.Annotations.ValidTimePairArray;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;

public class TimePairArrayValidator implements ConstraintValidator<ValidTimePairArray, ArrayList<TimePair>> {


    @Override
    public boolean isValid(ArrayList<TimePair> pairList, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        boolean valid=true;

            try {
                DailySchedule.getInstance(pairList);
            }catch (OperationConditionsFailedException e){
                constraintValidatorContext.buildConstraintViolationWithTemplate("Overlapping timepairs in array")
                        .addConstraintViolation();
                valid=false;
            }
        return valid;
    }

}
