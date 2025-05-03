package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.LatLng;

public class DirectionLegMixin {
    @JsonIgnore
    public LatLng startLocation;


    @JsonIgnore
    public LatLng endLocation;

    @JsonIgnore
    public String startAddress;

    @JsonIgnore
    public String endAddress;
}
