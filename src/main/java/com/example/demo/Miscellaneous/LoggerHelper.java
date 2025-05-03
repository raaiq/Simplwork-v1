package com.example.demo.Miscellaneous;


public class LoggerHelper {
    public static String getMethodCallingString(int parameters) {
        StringBuilder stringBuilder= new StringBuilder("Call to method {} ");
        if(parameters==0){
            return stringBuilder.toString();
        }
        stringBuilder.append("with parameters: ");
        while (parameters-- >0){
            stringBuilder.append("{}={}, ");
        }
        stringBuilder.delete(stringBuilder.length()-2,stringBuilder.length());
        return stringBuilder.toString();
    }

    public static final String methodReturnString="Object returned from method {}: {}";

    public static final String exceptionString="Exception caught in method:{} {}";

    public static final String invalidCodePath="Entry to invalid code path:{}";
}
