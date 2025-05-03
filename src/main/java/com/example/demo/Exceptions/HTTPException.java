package com.example.demo.Exceptions;

import org.springframework.http.HttpStatus;

public class HTTPException extends Exception{

    private final HttpStatus status;
    public HTTPException(String message, HttpStatus statusCode){
        super(message);
        status=statusCode;
    }

    public HTTPException(HttpStatus statusCode){
        super(statusCode.getReasonPhrase());
        status=statusCode;

    }

    public HttpStatus getHTTPStatus() {
        return status;
    }
}
