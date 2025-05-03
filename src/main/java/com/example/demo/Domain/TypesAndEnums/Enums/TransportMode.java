package com.example.demo.Domain.TypesAndEnums.Enums;

import com.google.maps.model.TravelMode;


public enum TransportMode {
    CAR(120,100,90,TravelMode.DRIVING),
    PUBLIC_TRANSIT(100,100,70,TravelMode.TRANSIT),
    BIKE(7,23,30,TravelMode.BICYCLING),
    WALK(1.5,5,20,TravelMode.WALKING);

    public final int maxTravelTimeMin;
    public final double maxTravelDistance;
    public final double averageSpeed;
    public final TravelMode googleMapsAnalog;

    TransportMode(double maxTravelDistance, double averageSpeed,int maxTravelTimeMin, TravelMode mode) {
        this.maxTravelDistance = maxTravelDistance;
        this.averageSpeed=averageSpeed;
        this.maxTravelTimeMin=maxTravelTimeMin;
        googleMapsAnalog =mode;
    }

    public double getDistanceCovered(int travelDurationMin){
        return travelDurationMin/60.0*averageSpeed;
    }

}
