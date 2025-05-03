package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.maps.model.*;

public abstract class DirectionStepMixin {

    @JsonIgnore
    public String htmlInstructions;
    public Distance distance;
    public Duration duration;
    public LatLng startLocation;
    public LatLng endLocation;

    @JsonIgnore
    public DirectionsStep[] steps;

    @JsonIgnore
    public EncodedPolyline polyline;
    public TravelMode travelMode;

    public TransitDetails transitDetails;

}
