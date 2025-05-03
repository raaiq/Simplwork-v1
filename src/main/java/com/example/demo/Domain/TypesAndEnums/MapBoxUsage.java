package com.example.demo.Domain.TypesAndEnums;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.Instant;

@Data
@Embeddable
public class MapBoxUsage {

    public Instant startDate;

    public int requests;

    @Transient
    public static final  int requestLimit= 100;

    @Transient
    public static final int daysToRefresh=7;


}
