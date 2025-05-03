package com.example.demo.Domain.Converters;

import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
public class StringMapConverter implements AttributeConverter<Map<String,String>,String> {
    @Override
    public String convertToDatabaseColumn(Map<String, String> stringStringMap) {
        try{
            return ViewDirector.convertToString(stringStringMap);
        } catch (
        JsonProcessingException e) {
            throw new RuntimeException("Error converting type Map<String,String> to Json",e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String s) {
        if(s==null){return new HashMap<>();}
        try{
            return ViewDirector.converttoObject(s, new TypeReference<>() {
            });
        } catch (
                JsonProcessingException e) {
            throw new RuntimeException("Error converting type Json to Map<String,String>",e);
        }
    }
}
