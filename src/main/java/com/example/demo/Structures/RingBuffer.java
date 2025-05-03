package com.example.demo.Structures;

import java.util.ArrayList;
import java.util.List;

public class RingBuffer<T> {

    Object[] array;
    int size;


    public RingBuffer(int size){
        array= new Object[size];
        this.size=size;
    }

    public T[] getRawArray(){
        return (T[]) array;
    }

    public T get(int index){

        index=adjustIndex(index);
        return (T)array[index];
    }

    public void set(int index, T element){
        index=adjustIndex(index);
        array[index]=element;
    }

    private int adjustIndex(int originalIndex){
        int newIndex= originalIndex%size;
        newIndex= originalIndex >=0 ? newIndex : size+newIndex;
        return newIndex;
    }
}
