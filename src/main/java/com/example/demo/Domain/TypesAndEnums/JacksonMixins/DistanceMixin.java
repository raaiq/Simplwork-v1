package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DistanceMixin {

    @JsonIgnore
    public String humanReadable;
}
