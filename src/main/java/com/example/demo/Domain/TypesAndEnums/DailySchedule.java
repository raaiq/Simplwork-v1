package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Exceptions.OperationConditionsFailedException;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DailySchedule extends LinkedList<TimePair>{


    public static DailySchedule getInstance(List<TimePair> source) throws OperationConditionsFailedException {
        DailySchedule schedule= new DailySchedule();
        try {

            for (TimePair pair:source) {
                schedule.insertElement(pair);
            }
        }catch (OperationConditionsFailedException e){
            throw new OperationConditionsFailedException("Source contains overlapping times");
        }
        return schedule;
    }
    public void insertElement(TimePair timePair) throws OperationConditionsFailedException {
        insertElementRecursive(this.listIterator(), timePair);
    }

    private void insertElementRecursive(ListIterator<TimePair>iterator, TimePair timePair) throws OperationConditionsFailedException {
        TimePair next=iterator.hasNext() ? iterator.next() : null,
                previous=iterator.hasPrevious() ? iterator.previous() : null;

        if(next == null){
            iterator.add(timePair);
            return;
        }
        if (previous==null ){
            if(timePair.endTime <= next.startTime){
            iterator.add(timePair);
            return;
            } else {
                iterator.next();
                insertElementRecursive(iterator,timePair);
                return;
            }
        }

        if(previous.endTime<=timePair.startTime && timePair.endTime <= next.startTime){
            iterator.add(timePair);
            return;
        }
        if(timePair.endTime >=next.endTime && timePair.startTime>=next.endTime){
            iterator.next();
            insertElementRecursive(iterator,timePair);
            return;
        }
        throw new OperationConditionsFailedException("Timepair overlaps with existing list");

    }
}
