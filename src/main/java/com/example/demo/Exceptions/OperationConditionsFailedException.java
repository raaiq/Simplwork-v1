package com.example.demo.Exceptions;

public class OperationConditionsFailedException extends Exception{

    public OperationConditionsFailedException(String message) {
        super(message);
    }

    public OperationConditionsFailedException(Throwable t){
        super(t);
    }
}
