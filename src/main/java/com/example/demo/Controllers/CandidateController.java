package com.example.demo.Controllers;

import com.example.demo.Domain.Views.CandidateProfileView;
import com.example.demo.Domain.TypesAndEnums.Compositions.CandidateUser;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Exceptions.ResourceAlreadyExistsException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.*;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Services.CandidateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

//TODO: Handle exceptions better
//TODO: Have proper error for JSON processing
//TODO: Have way to create documentation for rest endpoints
//TODO: Have separate object to get personal user info such as phoneNumber,age, gender

@Tag(name = "Candidate Profile Management", description = "For creating,modifying and viewing candidate profile associated with user")
@RestController
@Validated
@RequestMapping(path = "api/candidate",produces = "application/json")
public class CandidateController {


    private static final String className =CandidateController.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);


    private CandidateService candidateService;
    CandidateController(CandidateService candidateService){
        this.candidateService=candidateService;
    }

    @Operation(summary = "Creates a new candidate profile with it doesn't already exists",
                responses = {@ApiResponse(responseCode = "400",description = "Candidate profile already exists", content = { @Content(schema = @Schema())})})
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    CandidateProfileView createCandidate(@Valid @RequestBody CandidateUser requestBody) throws ResourceAlreadyExistsException, OperationConditionsFailedException, InternalException {
        CandidateProfileView view= ViewDirector.getCandidateProfilePrivateView(candidateService.addCandidateProfile(requestBody));
        return view;
    }

    @Operation(summary = "Gets candidate profile associated with user")
    @GetMapping
    CandidateProfileView getCandidate() throws ResourceNotFoundException {
        CandidateProfileView view= ViewDirector.getCandidateProfilePrivateView(candidateService.getDefaultCandidateProfile());

        return view;
    }
    //TODO: Rematch if travel times change
    @Operation(summary = "Modifies candidate associated with user")
    @PatchMapping(consumes = "application/json-patch+json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void modifyCandidate( @Valid @RequestBody JsonPatch changes) throws JsonPatchException, ResourceNotFoundException, JsonProcessingException, OperationConditionsFailedException, InternalException {
        candidateService.updateCandidateProfile(changes);
    }

    @DeleteMapping
    void deleteCandidate(){
        candidateService.deleteCandidateProfile();
    }


}
