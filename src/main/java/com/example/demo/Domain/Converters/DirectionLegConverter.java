package com.example.demo.Domain.Converters;

import com.example.demo.Domain.TypesAndEnums.CustomDirectionLeg;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.DirectionsLeg;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class DirectionLegConverter implements AttributeConverter<CustomDirectionLeg,String> {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    @Override
    public String convertToDatabaseColumn(CustomDirectionLeg directionsLeg) {
        try {
            return ViewDirector.convertToString(directionsLeg);
        } catch (JsonProcessingException e) {
            logger.error("Error converting Google DirectionLeg to JSON",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomDirectionLeg convertToEntityAttribute(String s) {
        try {
            return ViewDirector.converttoObject(s, CustomDirectionLeg.class);
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to Google DirectionLeg",e);
            throw new RuntimeException(e);
        }
    }
}
