package com.example.demo.Domain.Converters;

import com.example.demo.Domain.TypesAndEnums.TimePair;
import com.example.demo.Domain.TypesAndEnums.Timeslots;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.ArrayList;

@Converter(autoApply = true)
public class TimeSlotsConverter implements AttributeConverter<Timeslots<ArrayList<TimePair>>,String> {
    @Override
    public String convertToDatabaseColumn(Timeslots<ArrayList<TimePair>> attribute) {

        String aString="";
        for (int i=1; i<=7;i++) {

            ArrayList<TimePair> shift=attribute.get(DayOfWeek.of(i));

            for (int j=0; j<shift.size();j++) {
                TimePair timePair= shift.get(j);
                aString+= timePair.getStartTime()+","+timePair.getEndTime();

                if(j!=shift.size()-1){
                    aString+="|";
                }
            }

            if(i!=7){
            aString+="/";}
        }
        return aString;
    }

    @Override
    public Timeslots<ArrayList<TimePair>> convertToEntityAttribute(String dbData) {

        Timeslots<ArrayList<TimePair>> timeslots = new Timeslots();

        String[] firstArr= dbData.split("/");

        for (int i = 0; i < firstArr.length; i++) {
            String[] secondArr= firstArr[i].split("\\|");
            if(secondArr.length==1 && secondArr[0]==""){continue;}
            for (String timePairs:secondArr) {
                String[] times= timePairs.split(",");
                TimePair timePair= new TimePair(Short.parseShort(times[0]),Short.parseShort(times[1]));
                timeslots.get(DayOfWeek.of(i+1)).add(timePair);
            }
        }

        return timeslots;
    }



}
