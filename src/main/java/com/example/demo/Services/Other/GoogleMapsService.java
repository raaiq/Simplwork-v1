package com.example.demo.Services.Other;

import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.*;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Enums.TransportMode;
import com.example.demo.Exceptions.RouteNotFoundException;
import com.example.demo.Structures.RingBuffer;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.UnknownErrorException;
import com.google.maps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.Collectors;

//Singleton service not good for multithreading, have other solution
@Service
public class GoogleMapsService implements DisposableBean {

    private final GeoApiContext context;
    private static Logger logger= LoggerFactory.getLogger(GoogleMapsService.class.getName());


    //TODO:Might be redundant to check if dough is null
    public GoogleMapsService(@Value("${GOOGLE_DOUGH}") String dough){
        if(dough == null){
            logger.error("Environment variable GOOGLE_DOUGH not set");
            System.exit(-1);
        }
        context= new GeoApiContext.Builder().apiKey(dough).build();
    }


    public GeoApiContext getContext(){
        return context;
    }

    // -1 default value
    public int calculateCommuteTime(Location origin, Location destination, TransportMode travelMode, Optional<Instant> arrivalTime, Optional<Instant> departureTime) throws RouteNotFoundException, UnknownErrorException {
        var request=DistanceMatrixApi.newRequest(context);
        request.origins(origin.googleLatLng())
                .destinations(destination.googleLatLng());
        request.mode(travelMode.googleMapsAnalog);
        if(arrivalTime != null && arrivalTime.isPresent()){
            request.arrivalTime(arrivalTime.get());
        }else if (departureTime != null && departureTime.isPresent()){
            request.departureTime(departureTime.get());
        }
        DistanceMatrix result;
        try {
            result=request.await();
        } catch (Exception e) {

            String message= "Error in requesting distance matrix";

            logger.error(message,e);
            throw new UnknownErrorException(message);
        }
        DistanceMatrixElement element= result.rows[0].elements[0];
        DistanceMatrixElementStatus status= element.status;
        if(status == DistanceMatrixElementStatus.NOT_FOUND){
            throw  new RouteNotFoundException();
        }
        if(status !=DistanceMatrixElementStatus.OK){
            String message= "Unhandled status in distance matrix row[0] element[0]";
            logger.error(message+ ": {}",element.status);
            throw  new UnknownErrorException(message);
        }
        return (int)(result.rows[0].elements[0].duration.inSeconds/60);

    }
    //TODO: Have better way to calculate if candidate can commute to work. See if it's okay to either allow all shifts or clear all based on commute time
    public void getPublicTransitTimes(List<ShiftCompatibility> shiftCompatibilities, Location candidatelocation, Location jobLocation,int userMaxTravelTime, boolean manualMatch){
        ZonedDateTime localTime=ZonedDateTime.now(ZoneId.of("America/Toronto"));

        RingBuffer<CustomDirectionResult> arrivalCommuteCache= new RingBuffer<>(144),
                                                departureCommuteCache= new RingBuffer<>(144);
        int long_route_count=0,list_initial_size=shiftCompatibilities.size();

        for (int i=0;i<shiftCompatibilities.size();i++) {
            var compatibility=shiftCompatibilities.get(i);
            Shift shift=compatibility.getShift();
            int dayOfWeek= shift.getDayOfWeek();
            ZonedDateTime localTimeWithDay=localTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek)));

            boolean isWeekday=dayOfWeek != 6 && dayOfWeek!=7;

            CustomDirectionResult result;

            try {
                for (int j = 0; j < 2; j++) {
                    boolean isArrivalTime= j==0;

                    int t= isArrivalTime ? shift.getShiftTimes().startTime : shift.getShiftTimes().endTime;
                    t = t ==1440 ? 1439:t;
                    ZonedDateTime time= localTimeWithDay.withHour(t/60).withMinute(t%60);

                    result = getTransitDirections(candidatelocation, jobLocation, time, isArrivalTime, isArrivalTime ? arrivalCommuteCache:departureCommuteCache, userMaxTravelTime, isWeekday,manualMatch);

                    switch (result.getStatus()) {
                        case OK -> {
                            if(isArrivalTime){
                                compatibility.arrivalTransitInfo=result.getLeg();
                            }else{
                                compatibility.departureTransitInfo=result.getLeg();
                            }
                        }
                        case ROUTE_TOO_LONG -> {
                            if(isArrivalTime){
                                compatibility.arrivalTransitInfo=result.getLeg();
                            }else{
                                compatibility.departureTransitInfo=result.getLeg();
                            }
                            long_route_count++;
                        }
                        case NO_ROUTE_AVAILABLE -> {
                                shiftCompatibilities.clear();
                        }
                        case ROUTE_INFREQUENT -> {
                            shiftCompatibilities.remove(compatibility);
                        }

                    }
                    //TODO: Have better algorithm
                    if(long_route_count >=4 || long_route_count>list_initial_size){
                        shiftCompatibilities.clear();
                    }
                }



            } catch (IOException |InterruptedException | ApiException e) {
                logger.info("Aborting calculations of public transit times",e);
                break;
            }

        }

    }

        private CustomDirectionResult getTransitDirections(Location candidateLocation, Location jobLocation, ZonedDateTime time, boolean isArrivalTime, RingBuffer<CustomDirectionResult> cache, int maximumTravelTime, boolean isWeekday, boolean manualMatch) throws IOException, InterruptedException, ApiException {
            if(isWeekday){
                //TODO: Cache doesn't work
                CustomDirectionResult cacheResult=searchLegInCache(cache,time,isArrivalTime);
                if(cacheResult != null){
                    return cacheResult;
                }
            }
            CustomDirectionResult returnedResult=new CustomDirectionResult();
            DirectionsApiRequest request= new DirectionsApiRequest(context);
            request.mode(TravelMode.TRANSIT)
                    .alternatives(true)
                    .units(Unit.METRIC);

            if(isArrivalTime){
                request.origin(candidateLocation.googleLatLng())
                        .destination(jobLocation.googleLatLng())
                        .arrivalTime(Instant.from(time));
            }else{
                request.origin(jobLocation.googleLatLng())
                        .destination(candidateLocation.googleLatLng())
                        //TODO: Check if zonedDateTime properly converts to UTC
                        .departureTime(Instant.from(time));
            }
            DirectionsResult result=null;

            try {
               // System.out.println("Fetching transit times: "+request);
                result = request.await();
            } catch (InterruptedException | IOException | ApiException e) {
                logger.error("Error occurred requesting Google Map Directions : ",e);
                throw e;
            }

            List<DirectionsRoute> validRoutes=Arrays.stream(result.routes).filter((i)->i.legs[0].arrivalTime != null).toList();

            if(result.routes.length==0 || validRoutes.isEmpty()){
                //TODO: Have better way to tell if route doesn't exist
                    returnedResult.setStatus(CustomDirectionResult.CustomDirectionStatus.NO_ROUTE_AVAILABLE);
                return returnedResult;
            }
            DirectionsLeg directionsLeg=validRoutes.get(0).legs[0];
            int startInd,endInd;
            if(isArrivalTime){
                startInd= directionsLeg.arrivalTime.get(ChronoField.MINUTE_OF_DAY)/10;
                endInd= time.get(ChronoField.MINUTE_OF_DAY)/10;
            }else{
                startInd=time.get((ChronoField.MINUTE_OF_DAY))/10;
                endInd=directionsLeg.departureTime.get(ChronoField.MINUTE_OF_DAY)/10;
            }

            if(isWeekday){
                for (int i = startInd+1; i <endInd ; i++) {
                    CustomDirectionResult tempResult=new CustomDirectionResult();
                    tempResult.setStatus(CustomDirectionResult.CustomDirectionStatus.ROUTE_INFREQUENT);
                    cache.set(i,tempResult);
                }
            }
            if(!manualMatch){
                if(endInd -startInd > (isArrivalTime ? Constants.ARRIVAL_TIME_CANDIDATE_BUFFER.getIntValue() : Constants.DEPARTURE_TIME_BUFFER.getIntValue())){
                    returnedResult.setStatus(CustomDirectionResult.CustomDirectionStatus.ROUTE_INFREQUENT);
                    return returnedResult;
                }

                if(directionsLeg.duration.inSeconds/60.0 > maximumTravelTime*Constants.PUBLIC_TRANSIT_COMMUTE_TIME_MARGIN.getValue()){
                    returnedResult.setStatus(CustomDirectionResult.CustomDirectionStatus.ROUTE_TOO_LONG);
                }
            }

            returnedResult.setLeg(CustomDirectionLeg.convertFromDirectionLeg(directionsLeg));
            if(isWeekday){
                cache.set(isArrivalTime? startInd :endInd,returnedResult);
            }
            return returnedResult;

        }


        private CustomDirectionResult searchLegInCache(RingBuffer<CustomDirectionResult> cache, ZonedDateTime time, boolean isArrivalTime){
            int startingPoint, endingPoint;
            if(isArrivalTime){
                endingPoint= time.get(ChronoField.MINUTE_OF_DAY);
                startingPoint= endingPoint - Constants.ARRIVAL_TIME_CANDIDATE_BUFFER.getIntValue();
                endingPoint += Constants.ARRIVAL_TIME_EMPLOYER_BUFFER.getIntValue();
            }else {
                startingPoint = time.get(ChronoField.MINUTE_OF_DAY);
                endingPoint = startingPoint + Constants.DEPARTURE_TIME_BUFFER.getIntValue();
            }
            startingPoint /=10;
            endingPoint /=10;

            int notOKCounter=0;
            for (int i = startingPoint; i <= endingPoint; i++) {
                CustomDirectionResult r=cache.get(i);
                if(r != null ){
                    if(r.getStatus() == CustomDirectionResult.CustomDirectionStatus.OK){
                        return r;
                    }
                    notOKCounter++;
                }
            }
            if(notOKCounter == endingPoint -startingPoint){
                CustomDirectionResult result=new CustomDirectionResult();
                result.setStatus(CustomDirectionResult.CustomDirectionStatus.ROUTE_INFREQUENT);
                return result;
            }
            return null;
        }

    @Override
    public void destroy(){
        context.shutdown();
    }
}
