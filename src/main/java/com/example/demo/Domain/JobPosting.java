package com.example.demo.Domain;

import com.example.demo.Domain.TypesAndEnums.Enums.IndustryType;
import com.example.demo.Domain.TypesAndEnums.WeeklySchedule;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Validators.Annotations.ValidSchedule;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Structures.Sanitizer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


//TODO:Have annotation for sanitizing input
//TODO:Capture request information possibly when input is unsafe
//TODO:Handle case where rematching is required if required shifts or pay are changed
//TODO: Seperate
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
public class  JobPosting {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue
    long ID;

    @ManyToOne(fetch = FetchType.EAGER)
    Branch branch;

    //TODO: Make sure "Shifts_for_Job_Posting" entries are removed when jobs are deleted
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name= "Shifts_for_Job_Posting",
                joinColumns = {@JoinColumn(name = "Job_Posting_ID")},
                inverseJoinColumns = {@JoinColumn(name = "Shift_ID")})
    @NotNull
    @ValidSchedule
    List<Shift> shifts;

    @NotNull
    Boolean fixedSchedule;
    //TODO: Add constraint to have estimatedHours be non null if fixedSchedule is false
    @JsonSetter(nulls = Nulls.SKIP)
    Integer estimatedHours=0;
    @DecimalMin(value = "0.0",message = "Pay must a positive number")
    @NotNull
    Double pay;

    IndustryType industryType;

    @NonNull
    @NotBlank
    @NotNull
    @SafeString
    String positionTitle;

    @NonNull
    @NotBlank
    @Column(columnDefinition = "TEXT")
    String jobDescription;

    @Column(columnDefinition = "TEXT")
    String preferredJobExperience;


    @Column(columnDefinition = "TEXT")
    String benefits="";

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Past
    ZonedDateTime createdAt;


    public void copyFixedTraitsAndSanitizeFields(JobPosting posting){
        createdAt=posting.createdAt;
        ID=posting.ID;
        branch= posting.branch;
        pay=posting.pay;
        sanitizeStringFields();
    }

    public void initializeNewEntity(Branch branch){
        ID=0;
        this.branch= branch;
        //TODO: Change zone based on branch location
        createdAt= ZonedDateTime.now();
        preferredJobExperience = preferredJobExperience == null ? "" :preferredJobExperience;
        sanitizeStringFields();
        shifts.forEach((e)->e.initializeNewEntity(branch));
    }

    //TODO: Double check if each shift is unique
    public boolean requiresRematching(JobPosting other) throws OperationConditionsFailedException {
        WeeklySchedule schedule= WeeklySchedule.getInstance(shifts);
        WeeklySchedule otherSchedule= WeeklySchedule.getInstance(other.shifts);
        return !schedule.equals(otherSchedule);

    }

    public void sanitizeStringFields(){
        jobDescription= Sanitizer.sanitize(jobDescription,"jobDescription");
        preferredJobExperience = Sanitizer.sanitize(preferredJobExperience, "preferredJobExperience");
        benefits= Sanitizer.sanitize(benefits,"benefits");

    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobPosting posting = (JobPosting) o;

        if (ID != posting.ID) return false;
        if (branch.ID != posting.branch.ID) return false;
        if (Double.compare(posting.pay, pay) != 0) return false;
        if (!shifts.equals(posting.shifts)) return false;
        if (industryType != posting.industryType) return false;
        if (!positionTitle.equals(posting.positionTitle)) return false;
        if (!jobDescription.equals(posting.jobDescription)) return false;
        if (!Objects.equals(benefits, posting.benefits)) return false;
        if(!preferredJobExperience.equals(posting.preferredJobExperience)) return false;
        return createdAt.equals(posting.createdAt);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + (int) (branch.ID ^ (branch.ID >>> 32));
        result = 31 * result + shifts.hashCode();
        temp = Double.doubleToLongBits(pay);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + industryType.hashCode();
        result = 31 * result + positionTitle.hashCode();
        result = 31 * result + jobDescription.hashCode();
        result = 31 * result + preferredJobExperience.hashCode();
        result = 31 * result + (benefits != null ? benefits.hashCode() : 0);
        result = 31 * result + createdAt.hashCode();
        return result;
    }
}
