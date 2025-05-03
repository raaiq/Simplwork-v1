package com.example.demo.Domain.TypesAndEnums;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Have better way to implement constraint violations
@Schema(description = "Map keys can be any day of the week in capitalized form e.g MONDAY,TUESDAY,WEDNESDAY. Arrays in each day must not overlap",
        example = "{\"MONDAY\": [{" +
                "\"startTime\": 0," +
                "\"endTime\": 900},{" +
                "\"startTime\": 1000," +
                "\"endTime\": 1001}]," +
                "\"TUESDAY\": [{" +
                "\"startTime\": 9," +
                "\"endTime\": 80}]," +
                "\"WEDNESDAY\": [{" +
                "\"startTime\": 0," +
                "\"endTime\": 1200}]}")
public class Timeslots<U> extends EnumMap<DayOfWeek,U> implements Serializable {


    public Timeslots(){
        super((Map<DayOfWeek, ? extends U>) Arrays.stream(DayOfWeek.values()).map(i->Pair.of(i,new ArrayList<TimePair>())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }
}



