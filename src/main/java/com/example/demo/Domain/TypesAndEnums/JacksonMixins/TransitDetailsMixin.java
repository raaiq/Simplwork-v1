package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.maps.model.StopDetails;
import com.google.maps.model.TransitLine;

import java.time.ZonedDateTime;

public class TransitDetailsMixin {
    @JsonIgnore
    public StopDetails arrivalStop;
    @JsonIgnore
    public StopDetails departureStop;
    @JsonIgnore
    public ZonedDateTime arrivalTime;
    @JsonIgnore
    public ZonedDateTime departureTime;

    @JsonIgnore
    public String headsign;
    @JsonIgnore
    public long headway;
    @JsonIgnore
    public int numStops;
    public TransitLine line;
}
