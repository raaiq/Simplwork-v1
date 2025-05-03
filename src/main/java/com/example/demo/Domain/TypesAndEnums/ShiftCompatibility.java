package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.JacksonMixins.DirectionStepMixin;
import com.google.maps.model.DirectionsLeg;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
@ToString
public class ShiftCompatibility  implements Serializable {

    @ManyToOne
    @JoinColumn(name = "shift_id")
    Shift shift;

    public int minAfterShift; //Hours available from start of shift
    public int minTillEnd; //Hours unavailable till end of shift

    //TODO: Improve persistence of DirectionLeg, remove redundant entries, fields and shorter fields

    @Column(columnDefinition = "TEXT")
    public CustomDirectionLeg arrivalTransitInfo;

    @Column(columnDefinition = "TEXT")
    public CustomDirectionLeg departureTransitInfo;

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }
}
