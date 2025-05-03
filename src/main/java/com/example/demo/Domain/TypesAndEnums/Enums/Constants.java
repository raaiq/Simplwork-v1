package com.example.demo.Domain.TypesAndEnums.Enums;

//TODO:Fix naming
public enum Constants {

    DISTANCE_RADIUS_MULTIPLIER(1.15),
    MAXIMUM_TRAVEL_TIME(1),
    EMPLOYER_MAXIMUM_RADIUS(DISTANCE_RADIUS_MULTIPLIER.field*MAXIMUM_TRAVEL_TIME.field*100),
    MINIMUM_WAGE(15),
    PAY_MULTIPLIER(8),
    SHIFT_MULTIPLIER(100),
    DISTANCE_MULTIPLIER(10),
    DEFAULT_PAGE_SIZE(20),
    PUBLIC_TRANSIT_WAIT_TIME(40),
    ARRIVAL_TIME_EMPLOYER_BUFFER(15),
    ARRIVAL_TIME_CANDIDATE_BUFFER(40),

    DEPARTURE_TIME_BUFFER(60),
    COMMUTE_TIME_MARGIN(1.25),

    MAXIMUM_HOURS_BUFFER(1.20),

    PUBLIC_TRANSIT_COMMUTE_TIME_MARGIN(1.5);

    private final double field ;

    Constants(double value){
        field=value;
    }

    public double getValue(){
        return field;
    }

    public int getIntValue(){ return (int)field;}
}
