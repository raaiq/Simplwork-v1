package com.example.demo.Domain.Validators;

import com.example.demo.Domain.TypesAndEnums.WorkExperience;
import com.example.demo.Domain.Validators.Annotations.ValidExperience;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class WorkExperienceValidator implements ConstraintValidator<ValidExperience, WorkExperience> {
    @Override
    public boolean isValid(WorkExperience workExperience, ConstraintValidatorContext constraintValidatorContext) {

        boolean isValid=true;
        constraintValidatorContext.disableDefaultConstraintViolation();

        if (workExperience.getEndDate().isBefore(workExperience.getStartDate())) {
            isValid=false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("End date must be greater than start date").addConstraintViolation();

        }
        LocalDate currentDate=LocalDate.now();
        if(!workExperience.getStartDate().isBefore(currentDate)){
            isValid=false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("Start date must be before today").addPropertyNode("startDate").addConstraintViolation();
        }
        if(workExperience.getEndDate().isAfter(currentDate)){
            isValid=false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("End date must not be after today")
                    .addPropertyNode("endDate").addConstraintViolation();
        }

        return isValid;
    }
}
