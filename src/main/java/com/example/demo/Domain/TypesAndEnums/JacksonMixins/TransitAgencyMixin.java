package com.example.demo.Domain.TypesAndEnums.JacksonMixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TransitAgencyMixin {

    public String name;
    @JsonIgnore
    public String url;
    @JsonIgnore
    public String phone;
}
