package com.example.demo.Domain.Views;

import com.example.demo.Domain.TypesAndEnums.*;
import com.example.demo.Domain.TypesAndEnums.Enums.Gender;
import com.example.demo.Domain.TypesAndEnums.Enums.TransportMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateProfileView {
    Long ID;
    String candidateName;
    @Schema(example = "293-342-1453")
    String phoneNumber;
    @Schema(example = "example@email.com")
    String email;
    Gender gender;
    Short age;

    @Schema(example = "[{\"PUBLIC_TRANSIT\" : 90}, {\"WALK\" : 20}]")
    Map<TransportMode, Integer> maxTravelTimes;
    Set<WorkExperience> workHistory;
    Integer maximumHours;

    String fullAddress;

    Location location;
    Timeslots<ArrayList<TimePair>> availability;
    Boolean autoMatch;

}
