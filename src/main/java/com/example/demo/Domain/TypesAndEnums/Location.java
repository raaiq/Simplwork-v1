package com.example.demo.Domain.TypesAndEnums;

import com.example.demo.Domain.Converters.StringMapConverter;
import com.example.demo.Domain.Validators.Annotations.PostalCode;
import com.google.maps.model.LatLng;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Valid
//TODO: Account for longitude edge case where value near -180 are close to 180
public class Location {

    @NotNull
    @NonNull
    @DecimalMin(value = "-90.0", message = "Latitude must be greater or equal to -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be less or equal to 90")
    public Double latitude;

    @NotNull
    @NonNull
    @DecimalMin(value = "-180.0", message = "Longitude must be greater or equal to -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be less or equal to 180")
    public Double longitude;

    public String fullAddress;

    @Convert(converter = StringMapConverter.class)
    @Column(length = 512)
    public Map<String,String> addressComponents=new HashMap<>();


    public static Location getInstance(CarmenFeature feature){
        Location location= new Location();
        location.latitude= Objects.requireNonNull(feature.center()).latitude();
        location.longitude=feature.center().longitude();
        location.fullAddress=feature.placeName();
        feature.context().forEach(i->{
            location.addressComponents.put(i.id().toLowerCase().split("\\.")[0],i.text());
        });
        return location;
    }


    private final static double f= 1/298.257223563;
    private final static double eS= 2*f -f*f;
    private final static double a= 6378.137;
    private final static double b= 6356.752314245;

    public LatLng googleLatLng(){
        return new LatLng(latitude,longitude);
    }

    public static double kmToLongitude(double km,double referenceLatitude){
        double latRad=referenceLatitude/180*Math.PI;
        return km*(180*Math.pow(1-eS*Math.pow(Math.sin(latRad),2),.5))/(Math.PI*a*Math.cos(latRad));
    }

    public static double kmToLatitude(double km,double referenceLatitude){
        double latRad=referenceLatitude/180*Math.PI;
        return km/(111.132954-.559822*Math.cos(2*latRad)+.001175*Math.cos(4*latRad));
    }

    public static double longitudeToKm(double longitudeLength,double referenceLatitude){
        double latRad=referenceLatitude/180*Math.PI;
        return longitudeLength/(180*Math.pow(1-eS*Math.pow(Math.sin(latRad),2),.5))*(Math.PI*a*Math.cos(latRad));
    }

    public static double latitudeToKm(double latitudeLength,double referenceLatitude){
        double latRad=referenceLatitude/180*Math.PI;
        return latitudeLength*(111.132954-.559822*Math.cos(2*latRad)+.001175*Math.cos(4*latRad));
    }


    public  static double getDistance(Location a, Location b){
        double dLat= a.getLatitude()-b.getLatitude();
        double dLong= a.getLongitude()-b.getLongitude();
        double referenceLat=(a.getLatitude()+ b.getLatitude())/2;

        dLat= latitudeToKm(dLat, referenceLat);
        dLong= longitudeToKm(dLong,referenceLat);

        return Math.pow(dLat*dLat + dLong*dLong,.5);


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 && Double.compare(location.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationBounds{
        public double latitudeLow,latitudeHigh,longitudeLow,longitudeHigh;
    }
}
