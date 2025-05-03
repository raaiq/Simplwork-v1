package com.example.demo.Exceptions;

public class ResourceAlreadyExistsException extends Exception {
    public ResourceAlreadyExistsException(String s) {
        super(s);
    }

    public ResourceAlreadyExistsException(Throwable t){
        super(t);
    }
    public ResourceAlreadyExistsException(){}
}
