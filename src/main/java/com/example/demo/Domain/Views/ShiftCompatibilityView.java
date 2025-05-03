package com.example.demo.Domain.Views;

import com.example.demo.Domain.TypesAndEnums.CustomDirectionLeg;
import com.example.demo.Domain.TypesAndEnums.ShiftCompatibility;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.maps.model.DirectionsLeg;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShiftCompatibilityView {

    UUID shiftID;
    ShiftView shift;

    public Integer minAfterShift;
    public Integer minTillEnd;
    @Schema(oneOf = {Integer.class})
    //TODO: Marshall only important details
    public Object arrivalTransitInfo , departureTransitInfo;

    public ShiftCompatibilityView(ShiftCompatibility compatibility,boolean verbose){
        minAfterShift =compatibility.minAfterShift;
        minTillEnd=compatibility.minTillEnd;
        CustomDirectionLeg arrivalLeg = compatibility.arrivalTransitInfo,
                      departureLeg = compatibility.departureTransitInfo;
        if(verbose){
            shift= new ShiftView(compatibility.getShift());
            arrivalTransitInfo=arrivalLeg;
            departureTransitInfo=departureLeg;

        }else {
            shiftID=compatibility.getShift().getID();
            arrivalTransitInfo = arrivalLeg != null ? String.valueOf(arrivalLeg.durationInSec/60) : null;
            departureTransitInfo = departureLeg != null ? String.valueOf(departureLeg.durationInSec/60) : null;
        }
    }
}
