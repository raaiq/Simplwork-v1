package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Domain.Validators.Annotations.ValidTimePair;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Schema(description = "Start time must be less than end time")
@ValidTimePair
public class TimePair implements Serializable {

    //min format from 0 to 24 hours
    @NotNull
    public Short startTime = 0;
    @NotNull
    public Short endTime = 0;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePair timePair = (TimePair) o;

        if (!startTime.equals(timePair.startTime)) return false;
        return Objects.equals(endTime, timePair.endTime);
    }

    @Override
    public int hashCode() {
        int result = startTime;
        result = 31 * result + (int) endTime;
        return result;
    }

}
