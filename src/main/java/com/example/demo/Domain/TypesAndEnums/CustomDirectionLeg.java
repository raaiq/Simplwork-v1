package com.example.demo.Domain.TypesAndEnums;

import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;
import com.google.maps.model.LatLng;

import java.time.ZonedDateTime;

public class CustomDirectionLeg {
    public long distanceInMeters;
    public long durationInSec;
    public long durationInTrafficInSec;
    public ZonedDateTime arrivalTime;
    public ZonedDateTime departureTime;
    public Location startLocation;
    public Location endLocation;
    public String startAddress;
    public String endAddress;


    public static CustomDirectionLeg convertFromDirectionLeg(DirectionsLeg leg){
        var customLeg= new CustomDirectionLeg();
        customLeg.arrivalTime=leg.arrivalTime;
        customLeg.departureTime=leg.departureTime;
        customLeg.distanceInMeters= leg.distance.inMeters;
        customLeg.durationInSec= leg.duration.inSeconds;
        customLeg.startLocation= new Location(leg.startLocation.lat,leg.startLocation.lng);
        customLeg.endLocation= new Location(leg.endLocation.lat, leg.endLocation.lng);
        customLeg.startAddress= leg.startAddress;
        customLeg.endAddress= leg.endAddress;
        return customLeg;

    }
}
