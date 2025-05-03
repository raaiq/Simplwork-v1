package com.example.demo.Exceptions;

public class InternalException extends Exception{
    public InternalException(String message){
        super(message);
    }

    public InternalException(Throwable t){
        super(t);
    }

    public InternalException(String message, Throwable t){
        super(message,t);
    }
}
