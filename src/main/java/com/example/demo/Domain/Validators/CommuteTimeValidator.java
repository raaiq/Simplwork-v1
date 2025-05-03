package com.example.demo.Domain.Validators;

import com.example.demo.Domain.TypesAndEnums.Enums.TransportMode;
import com.example.demo.Domain.Validators.Annotations.ValidCommuteTimes;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class CommuteTimeValidator implements ConstraintValidator<ValidCommuteTimes, Map<TransportMode,Integer>> {
    @Override
    public boolean isValid(Map<TransportMode, Integer> map, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        boolean valid=true;
        if(map.isEmpty()){
            constraintValidatorContext.buildConstraintViolationWithTemplate("Commute times must contain at least one transport method").addConstraintViolation();
            return false;
        }
        for (Map.Entry<TransportMode,Integer> e:map.entrySet()) {
            if(e.getValue() == null || e.getValue()<=0 || e.getValue()>=10e4){
                constraintValidatorContext.buildConstraintViolationWithTemplate("Commute time must a positive integer below 10e4").addBeanNode().inIterable().atKey(e.getKey()).addConstraintViolation();
                valid=false;
            }
        }
        return valid;
    }
}
