package com.example.demo.Services.Other;

import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.example.demo.Domain.TypesAndEnums.MapBoxUsage;
import com.example.demo.Domain.UserInfo;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Repositories.UserRepo;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

//TODO:Have better way than proxying requests
@Service
public class MapBoxService {

    private final String token;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private UserRepo userRepo;
    private int requestLimit;

    MapBoxService(@Value("${MAPBOX_DOUGH}") String token, @Value("${geocoding.request_limit:200}") int requestLimit, UserRepo userRepo){
        this.token=token;
        this.userRepo=userRepo;
        this.requestLimit=requestLimit;
    }

    @PreAuthorize("hasRole('USER')")
    public String autoCompleteQuery(String query) throws OperationConditionsFailedException,InternalException {

        checkMapboxUsage();

        Point proximityPoint= Point.fromLngLat(-79.597656,43.678709);
        MapboxGeocoding request= MapboxGeocoding.builder().accessToken(token)
                                                            .query(query)
                                                            .autocomplete(true)
                                                            .fuzzyMatch(true)
                                                            .proximity(proximityPoint)
                                                            .build();
        try {
            Response<GeocodingResponse> response= request.executeCall();
            if(response.isSuccessful()){
                return response.body().toJson();
            }
            logger.warn("Unsuccessful request to MapBox Geocode API: {}",response.raw());

        } catch (IOException e) {
            logger.error("Failed request to MapBox Geocode API:",e);
        }
        throw new InternalException("Mapbox search api request failed");
    }


    @PreAuthorize("hasRole('USER')")
    public Location getMapboxLocation(Location location) throws OperationConditionsFailedException, InternalException {
        checkMapboxUsage();

        MapboxGeocoding request= MapboxGeocoding.builder().accessToken(token)
                .query(Point.fromLngLat(location.longitude, location.latitude))
                .build();

        try {
            Response<GeocodingResponse> response= request.executeCall();
            if(response.isSuccessful()){
                assert response.body() != null;
                return Location.getInstance(response.body().features().get(0));
            }
            logger.warn("Unsuccessful request to MapBox Reverse Geocode API: {}",response.raw());

        } catch (IOException e) {
            logger.error("Failed request to MapBox Reverse Geocode API:",e);
        }
        throw new InternalException("Mapbox reverse search api request failed");

    }

    private void checkMapboxUsage() throws OperationConditionsFailedException {
        UserInfo user=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        MapBoxUsage usage= user.getGeocodingUsage();
        Instant startDate=usage.startDate;
        Instant endDate=Instant.now();
        if( startDate==null || Duration.between(startDate,endDate).toDays() > MapBoxUsage.daysToRefresh){
            usage.requests=0;
            usage.startDate=endDate;
        }
        if(usage.requests++ > requestLimit){
            throw new OperationConditionsFailedException("Request limit exceeded");
        }

        userRepo.save(user);
    }




//
//    @PreAuthorize("hasRole('USER')")
//    Location geocodeAddress(String address){
//        MapboxGeocoding request= MapboxGeocoding.builder()
//                                                .accessToken(token)
//                                                .mode(GeocodingCriteria.MODE_PLACES_PERMANENT)
//                                                .query(address)
//                                                .build();
//
//
//    }
}
