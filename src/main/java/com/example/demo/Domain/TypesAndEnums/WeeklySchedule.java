package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Domain.Shift;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import org.springframework.data.util.Pair;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

//TODO:Replace availability with weekly schedule
public class WeeklySchedule extends EnumMap<DayOfWeek,DailySchedule> {
    public WeeklySchedule() {
        super(Arrays.stream(DayOfWeek.values()).map(i-> Pair.of(i,new DailySchedule())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }


    public static WeeklySchedule getInstance(List<Shift> source) throws OperationConditionsFailedException {
        WeeklySchedule schedule=new WeeklySchedule();
        try {
            for (Shift shift:source) {
                    schedule.get(DayOfWeek.of(shift.getDayOfWeek())).insertElement(shift.getShiftTimes());
            }
        } catch (OperationConditionsFailedException e) {
            throw new OperationConditionsFailedException("Overlapping shifts in source");
        }
        return schedule;
    }

    public static WeeklySchedule getInstance(Timeslots<List<TimePair>> source) throws OperationConditionsFailedException {
        WeeklySchedule schedule = new WeeklySchedule();
        try {
            for (int i = 0; i < source.size(); i++) {
                schedule.put(DayOfWeek.of(i+1),DailySchedule.getInstance(source.get(i)));
            }
        }catch (OperationConditionsFailedException e){
            throw new OperationConditionsFailedException("Overlapping shifts in source");
        }
        return schedule;
    }
}
