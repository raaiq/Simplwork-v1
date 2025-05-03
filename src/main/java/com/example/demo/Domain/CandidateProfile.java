package com.example.demo.Domain;


import com.example.demo.Domain.TypesAndEnums.*;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Enums.TransportMode;
import com.example.demo.Domain.Validators.Annotations.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

//TODO: See why lazy fetching fails for collections
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE,force = true)

public class CandidateProfile {

    @Transient
    public static final String DEFAULT_CANDIDATE_PROFILE = "Default_Profile";

    @Id
    @GeneratedValue
    @JsonIgnore
    long ID;

    @JsonIgnore
    @NonNull
    @SafeString
    String profileName;

    @Embedded
    @ElementCollection(fetch = FetchType.EAGER)
    Set< @Valid WorkExperience> workHistory;

    //Km
    @JsonIgnore
    @Min(value = 0,message = "Distance must be positive integer in km")
    Integer comfortableDistance;

    //TODO:Use field
    @DecimalMin(value = "0.0", message = "Pay must be positive number")
    Double  minimumPay;


    @Min(value = 0, message = "Maximum hours must be greater or equal to 0")
    @NotNull
    Integer maximumHours;
    @Embedded
    @NotNull
    @NonNull
    @Valid
    Location location;

    // TODO: Ask user if they're available for overnight shift
    @NotNull
    @NonNull
    @Valid
    @Column(columnDefinition = "TEXT")
    Timeslots<@ValidTimePairArray ArrayList<@Valid TimePair>> availability;

    //Weight in pounds
    @Min(value = 0,message = "Weight must be a positive integer")
    Integer maxLiftWeight;

    // 0 latitudeLow
    // 1 latitudeHigh
    // 2 longitudeLow
    // 3 longitudeHigh
    @JsonIgnore
    @Embedded
    Location.LocationBounds locationBoundaries;

    //TODO: Have better way to document key/ value defaults and constraints
    @Schema(description = "Keys can be one PUBLIC_TRANSIT, WALK, BIKE or CAR. Values are travel times in min",
            defaultValue = "{\"BIKE\": 22," +
                    "\"PUBLIC_TRANSIT\": 90," +
                    "\"CAR\": 20," +
                    "\"WALK\": 300}")
    @ElementCollection(fetch = FetchType.EAGER)
    @ValidCommuteTimes
    Map<TransportMode, Integer> maxTravelTimes;

    @NotNull
    Boolean autoMatch;

    /*
     * Document purpose of methods
     * **/

    // Purpose of method is to not allow crucial variables to be modified by patch requests. i.e. ID
    public void copyFixedTraitsAndSanitizeFields(CandidateProfile profile){
        profileName=profile.getProfileName();
        ID=profile.getID();
        locationBoundaries=profile.locationBoundaries;
        sanitizeStringFields();
    }

    public void initializeNewEntity(){
        ID=0;
        profileName=DEFAULT_CANDIDATE_PROFILE;

        double radius= maxTravelTimes.entrySet().stream().map(i->i.getKey().getDistanceCovered(i.getValue()))
                                                    .max(Double::compareTo).get();
        radius= Math.max(radius,50);
        radius= radius* Constants.DISTANCE_RADIUS_MULTIPLIER.getValue();

        double latRad= Location.kmToLatitude(radius,location.getLatitude()),
                longRad= Location.kmToLongitude(radius,location.getLatitude());

        locationBoundaries=new Location.LocationBounds(location.latitude-latRad,
                location.latitude+latRad,
                location.longitude-longRad,
                location.longitude+longRad);
        sanitizeStringFields();
    }

    public void sanitizeStringFields(){
        if(workHistory !=null){
            workHistory= workHistory.stream().map(WorkExperience::sanitize).collect(Collectors.toSet());
        }
    }
    //TODO: Use WeeklySchedule to compare availability
    public boolean requiresRematching(CandidateProfile other){
        return !(location.equals(other.location) && availability.equals(other.availability) && maxTravelTimes.equals(other.maxTravelTimes));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CandidateProfile profile = (CandidateProfile) o;

        if (ID != profile.ID) return false;
        if (comfortableDistance != profile.comfortableDistance) return false;
        if (Double.compare(profile.minimumPay, minimumPay) != 0) return false;
        if (maximumHours != profile.maximumHours) return false;
        if (maxLiftWeight != profile.maxLiftWeight) return false;
        if (autoMatch != profile.autoMatch) return false;
        if (!profileName.equals(profile.profileName)) return false;
        if (!Objects.equals(workHistory, profile.workHistory)) return false;
        if (!location.equals(profile.location)) return false;
        if (!availability.equals(profile.availability)) return false;
        if (!Objects.equals(locationBoundaries, profile.locationBoundaries))
            return false;
        return Objects.equals(maxTravelTimes, profile.maxTravelTimes);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + profileName.hashCode();
        result = 31 * result + (workHistory != null ? workHistory.hashCode() : 0);
        result = 31 * result + comfortableDistance;
        temp = Double.doubleToLongBits(minimumPay);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + maximumHours;
        result = 31 * result + location.hashCode();
        result = 31 * result + availability.hashCode();
        result = 31 * result + maxLiftWeight;
        result = 31 * result + (locationBoundaries != null ? locationBoundaries.hashCode() : 0);
        result = 31 * result + (maxTravelTimes != null ? maxTravelTimes.hashCode() : 0);
        result = 31 * result + (autoMatch ? 1 : 0);
        return result;
    }
}
