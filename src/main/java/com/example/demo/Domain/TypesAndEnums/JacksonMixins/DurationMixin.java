package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DurationMixin {

    @JsonIgnore
    public String humanReadable;
}
