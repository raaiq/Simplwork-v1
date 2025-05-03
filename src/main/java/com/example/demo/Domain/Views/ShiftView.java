package com.example.demo.Domain.Views;

import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.TimePair;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShiftView {
    UUID ID;
    Integer dayOfWeek;
    TimePair shiftTimes;

    BranchView branch;

    //TODO: Change back to more accurate times
    public ShiftView(Shift shift){
        ID=shift.getID();
        dayOfWeek= shift.getDayOfWeek();
        shiftTimes= shift.getShiftTimes();
        shiftTimes.endTime= (short) (shiftTimes.endTime/60);
        shiftTimes.endTime= (short) (shiftTimes.endTime*60);
        shiftTimes.startTime= (short) (shiftTimes.startTime/60);
        shiftTimes.startTime= (short)(shiftTimes.startTime*60);

    }
}
