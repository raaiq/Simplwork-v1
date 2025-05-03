package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.maps.model.TransitAgency;
import com.google.maps.model.Vehicle;

public class TransitLineMixin {
    @JsonIgnore
    public String name;
    public String shortName;
    public String color;
    public TransitAgency[] agencies;
    @JsonIgnore
    public String url;
    @JsonIgnore
    public String icon;
    @JsonIgnore
    public String textColor;
    @JsonIgnore
    public Vehicle vehicle;
}
