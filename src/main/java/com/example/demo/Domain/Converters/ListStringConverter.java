package com.example.demo.Domain.Converters;

import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter(autoApply = true)
public class ListStringConverter implements AttributeConverter<List<String>,String> {

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        try {
            return ViewDirector.convertToString(strings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting type List<String> to Json",e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        try {
            return ViewDirector.converttoObject(s, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting type JSON to List<String>",e);
        }
    }
}
