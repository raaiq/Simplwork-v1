package com.example.demo.Domain;

import com.example.demo.Domain.TypesAndEnums.TimePair;
import com.example.demo.Domain.Validators.Annotations.ValidTimePair;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Shift implements Comparable<Shift>{
    @Id
    @GeneratedValue
    UUID ID;

    @Min(value = 1,message = "Day of week begin at 1")
    @Max(value = 7)
    @NotNull
    Integer dayOfWeek;


    //TODO:Maybe valid annotation can be embedded at class level
    @Embedded
    @ValidTimePair
    TimePair shiftTimes;

    @ManyToOne
    Branch branch;

    public void initializeNewEntity(Branch branch){
        this.branch=branch;
        ID=null;

    }

    public void copyFixedTraitsAndSanitizeFields(Shift shift){
        ID=shift.ID;
        branch=shift.branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shift shift = (Shift) o;

        if (ID != shift.ID) return false;
        if (!Objects.equals(shiftTimes, shift.shiftTimes)) return false;
        return Objects.equals(branch, shift.branch);
    }

    @Override
    public int hashCode() {

        int result = ID != null ? ID.hashCode() : 0;
        result = 31 * result + (shiftTimes != null ? shiftTimes.hashCode() : 0);
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        return result;
    }

    //Only compares the starting time of shifts
    @Override
    public int compareTo(@NonNull Shift o) {
        return dayOfWeek*24*60+shiftTimes.startTime -(o.dayOfWeek*24*60+o.shiftTimes.startTime);
    }
}
