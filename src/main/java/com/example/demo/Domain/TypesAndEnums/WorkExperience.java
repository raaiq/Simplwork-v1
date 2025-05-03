package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Validators.Annotations.ValidExperience;
import com.example.demo.Structures.Sanitizer;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Objects;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
// TODO: Store reference to the employer, instead of string
@ValidExperience
public class WorkExperience {
    @SafeString
    @NotBlank
    String positionTitle;
    @SafeString
    @NotBlank
    String companyName;
    @NotBlank
    @Column(columnDefinition = "TEXT")
    String details;

    @NotNull
    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate startDate;

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate endDate;


    public WorkExperience sanitize(){
        details= Sanitizer.sanitize(details,"details");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkExperience that = (WorkExperience) o;

        if (!positionTitle.equals(that.positionTitle)) return false;
        if (!companyName.equals(that.companyName)) return false;
        if (!Objects.equals(details, that.details)) return false;
        if (!startDate.equals(that.startDate)) return false;
        return Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        int result = positionTitle.hashCode();
        result = 31 * result + companyName.hashCode();
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + startDate.hashCode();
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
