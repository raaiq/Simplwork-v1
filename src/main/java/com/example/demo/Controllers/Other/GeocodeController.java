package com.example.demo.Controllers.Other;

import com.example.demo.Domain.Validators.Annotations.QueryString;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Services.Other.MapBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Geocoder")
@RestController
@Validated
@RequestMapping(path = "/api/geocoding", produces = "application/json")
public class GeocodeController {

    @Autowired
    private MapBoxService mapBoxService;

    //TODO: Have better way to document schema
    @Operation(summary = "Autocompletes address string and provides location information such as longitude and latitude")
    @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(example = "Schema at https://docs.mapbox.com/api/search/search-box/#response-get-suggested-results",description = "Schema at https://docs.mapbox.com/api/search/search-box/#response-get-suggested-results"))})
    @GetMapping
    //TODO: Allow comma
    String suggestAddress(@Parameter(description = "Address string") @RequestParam @QueryString String query) throws OperationConditionsFailedException, InternalException {

        return mapBoxService.autoCompleteQuery(query);
    }
}
