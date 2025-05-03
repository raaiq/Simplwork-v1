package com.example.demo.Domain.Validators;

import com.example.demo.Domain.TypesAndEnums.TimePair;
import com.example.demo.Domain.Validators.Annotations.ValidTimePair;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimePairValidator implements ConstraintValidator<ValidTimePair, TimePair> {
    @Override
    public boolean isValid(TimePair timePair, ConstraintValidatorContext constraintValidatorContext) {

        final String rangeMessage="Time entry must be between 0 and 1440 inclusive";
        constraintValidatorContext.disableDefaultConstraintViolation();
        boolean valid=true;

        if(!validTime(timePair.getStartTime())){
            constraintValidatorContext.buildConstraintViolationWithTemplate(rangeMessage)
                    .addPropertyNode("endTime").addConstraintViolation();
            valid=false;
        }
        if(!validTime(timePair.getEndTime())){
            constraintValidatorContext.buildConstraintViolationWithTemplate(rangeMessage)
                    .addPropertyNode("startTime").addConstraintViolation();
            valid=false;
        }

        if(timePair.getStartTime() >= timePair.getEndTime()){
            constraintValidatorContext.buildConstraintViolationWithTemplate("endTime must be greater than startTime").addConstraintViolation();
            valid=false;
        }
        return valid;
    }

    private boolean validTime(int time){
        return time >=0 && time <= 1440;
    }
}
