package com.example.demo.Domain.TypesAndEnums;

import com.google.maps.model.DirectionsLeg;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class CustomDirectionResult {

    CustomDirectionLeg leg;

    CustomDirectionStatus status= CustomDirectionStatus.OK;


    public enum CustomDirectionStatus{
        ROUTE_INFREQUENT,
        NO_ROUTE_AVAILABLE,
        ROUTE_TOO_LONG,
        OK

    }
}

