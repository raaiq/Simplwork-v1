package com.example.demo.Exceptions;

public class AuxiliaryResourceNotFoundException extends Exception {

    public AuxiliaryResourceNotFoundException(String message){
        super(message);
    }

    public AuxiliaryResourceNotFoundException(Throwable t){
        super(t);
    }
}
