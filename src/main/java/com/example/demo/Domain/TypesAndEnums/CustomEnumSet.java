package com.example.demo.Domain.TypesAndEnums;

import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO: Complete class
public class CustomEnumSet<T extends Enum<T>> {

    private long set=0;

    @Transient
    private Map<Integer,T> ordinalMap;

    public CustomEnumSet(T ... elements){
        addEnums(elements);
    }

    public CustomEnumSet(){

    }

    private void checkOrdinalWithinRange(int ord){
        if(ord >63){
            throw new IndexOutOfBoundsException("Ordinal of enum is greater than 63");
        }
    }

    public void addEnums(T ... ens){
        for (T e:ens
             ) {
            addEnum(e);
        }
    }
    public void addEnum(T en){
        int ordinal= en.ordinal();
        checkOrdinalWithinRange(ordinal);
        set |= 2L << ordinal;
    }

    public boolean checkIfExists(T en){
        return (set >> en.ordinal() & 1) == 1;
    }

//    public List<T> getAsList(){
//        int maxArraySize=(int)(Math.log10(set)/Math.log10(2));
//        ArrayList<T> enumList= new ArrayList<>(maxArraySize);
//        int count=0;
//        while (count++ < maxArraySize+1){
//        }
//    }
}
