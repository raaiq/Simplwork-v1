package com.example.demo.Configuration;

import com.example.demo.Domain.TypesAndEnums.JacksonMixins.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.maps.model.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Strategy;

@Configuration
public class GeneralConfig {
    // Logbook Config

    //TODO: Have better alternative, logbook doesn't handle errors to dispatcher servlets
    @Bean
    Strategy logResponseBodyIfStatus(@Value("${logbook.minimum-status:400}") int status){
        return new ResponseBodyIfStatusAtLeastStrategy(status);
    }


    // OpenAPI Config
    @Bean
    public OpenAPI openAPI(){
        //TODO: Add contact info, server URL, etc
        Info info= new Info();
        info.setTitle("Simplwork API");
        info.setDescription("ThiS API serves an interface to the Simplwork platform. " +
                "Allowing for user management, candidate/employer creation, job posting management,job matching and related auxiliary operations");

        return new OpenAPI().info(info);

    }


    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.mixIn(DirectionsLeg.class, DirectionLegMixin.class)
                                 .mixIn(DirectionsStep.class, DirectionStepMixin.class)
                                 .mixIn(TransitDetails.class,TransitDetailsMixin.class)
                                 .mixIn(Duration.class, DurationMixin.class)
                                 .mixIn(Distance.class, DistanceMixin.class)
                                 .mixIn(TransitLine.class, TransitLineMixin.class)
                                 .mixIn(TransitAgency.class, TransitAgencyMixin.class)
                                 .serializationInclusion(JsonInclude.Include.NON_NULL)
                                 .modulesToInstall(new JavaTimeModule());

    }
}
