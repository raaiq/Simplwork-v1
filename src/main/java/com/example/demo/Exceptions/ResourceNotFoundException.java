package com.example.demo.Exceptions;

public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Throwable t){
        super(t);
    }
}
