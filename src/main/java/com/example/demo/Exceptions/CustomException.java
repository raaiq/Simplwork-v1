package com.example.demo.Exceptions;

import com.example.demo.Services.CandidateService;

    public class CustomException extends Exception{

    public CustomException(String e){
        super(e);
    }

    public CustomException(){
        super();
    }
}
