package com.example.demo.Controllers;


import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchSortBy;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Views.BranchView;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Domain.Views.JobPostingView;
import com.example.demo.Domain.Views.MatchView;
import com.example.demo.Domain.Views.PostingEmployerView;
import com.example.demo.Exceptions.*;
import com.example.demo.Miscellaneous.LoggerHelper;
import com.example.demo.Repositories.UserRepo;
import com.example.demo.Services.EmployerPostingService;
import com.example.demo.Services.JobPostingService;
import com.example.demo.Services.Other.EmployerBranchPairService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//TODO:Add sorting criteria to pageable
@Tag(name = "Job Posting Management",description = "For CRUD operations of job postings and reviewing ,processing potential candidates")
@RestController
@Validated
@RequestMapping(path = "api/employer/postings",produces = "application/json")
public class EmployerPostingController {

    @Autowired
    private EmployerPostingService empPostingService;
    @Autowired
    private JobPostingService jobService;
    @Autowired
    private EmployerBranchPairService pairService;

    @Autowired
    private UserRepo userRepo;

    private static final String className=EmployerPostingController.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);

    @Operation(summary = "Provides a list of job postings associated with employer",
            description = "Privileged access required")
    @GetMapping(path = "/overview")
    @ResponseStatus(HttpStatus.OK)
    List<PostingEmployerView> getPostings(@RequestParam String employerName,
                                          @Parameter(description = "Filters the list to job postings associated with the specified branch")@RequestParam Optional<@SafeString String> branch,
                                          @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(0) Integer> pageSize,
                                          @RequestParam(defaultValue = "0") @Min(0) Integer pageNo) throws AuxiliaryResourceNotFoundException, IllegalAccessException {

        Pageable jobsPageable= PageRequest.of(  pageNo,
                                                pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue()),
                                                Sort.by("createdAt").descending());

       return empPostingService.getPostingsOverview(employerName,branch,jobsPageable);
    }

    @Operation(summary = "Provides list of matches by employer match status and posting id",
                description = "Privileged access required")
    @GetMapping(path = "/match/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<MatchView> getMatches(@PathVariable(name = "id") long postingId,
                                      @RequestParam MatchEmployerStatus status,
                                      @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(0) Integer> pageSize,
                                      @RequestParam(defaultValue = "0") @Min(0) Integer pageNo,
                                      @Parameter(description = "Default value is SHIFT_SCORE") @RequestParam Optional<MatchSortBy> sortBy,
                                      @Parameter(description = "Default value is DESCENDING")@RequestParam Optional<Sort.Direction> sortDirection) throws IllegalAccessException, AuxiliaryResourceNotFoundException {

        Pageable pageable= PageRequest.of(  pageNo,
                                            pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue()),
                                            sortDirection.orElse(Sort.Direction.DESC),
                                            sortBy.orElse(MatchSortBy.SHIFT_SCORE).JPASimpleAnalog);

        EmployerBranch pair= null;
        try {
            pair = pairService.getPairFromPosting(postingId);
        } catch (ResourceNotFoundException e) {
            throw new AuxiliaryResourceNotFoundException("Unable to find posting with ID "+ postingId);
        }
        List<Match> matches=empPostingService.getMatches(pair,postingId,status,pageable);
        //TODO: Have better solution
        var matchPairs=matches.stream().map(i->
           Pair.of(i,userRepo.findByCandidatesProfiles_ID(i.getCandidateProfile().getID()).orElseThrow(()->new RuntimeException("Unable to find candidate profile associated with match")))
        ).collect(Collectors.toList());

        return ViewDirector.getMatchEmployerView(matchPairs);

    }
    @Operation(summary = "Creates a new job posting",
            description = "Privileged access required")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    JobPostingView createPosting(@RequestParam @SafeString String employerName,
                                 @RequestParam @SafeString String branchName,
                                 @Valid @RequestBody JobPosting posting) throws ResourceNotFoundException {

        EmployerBranch pair=pairService.getPair(employerName,branchName);
        //TODO: Check if shifts are automatically saved
        JobPosting savedPosting=jobService.savePosting(pair,posting);

        JobPostingView view=ViewDirector.getJobPostingEmployerView(savedPosting);

        return view;
    }

    @Operation(summary = "Gets information about job posting")
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    JobPostingView getJobPosting(@Parameter(description = "ID associated with job posting") @PathVariable(name = "id") long id) throws ResourceNotFoundException {
        JobPosting posting=jobService.findPosting(id);
        JobPostingView view= ViewDirector.getJobPostingCandidateView(posting);
        return view;
    }


    //TODO: Maybe combine overview and get endpoints
    @Operation(summary = "Gets list of job postings for employer")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<JobPostingView> getPostingsOverview(@RequestParam @SafeString String employerName,
                                             @Parameter(description = "Filters the the list to postings associated with branch") @RequestParam Optional<@SafeString String> branch,
                                             @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(0) Integer> pageSize,
                                             @RequestParam(defaultValue = "0") @Min(0) Integer pageNo
                                             ) {
        Pageable pageable= PageRequest.of(  pageNo,
                                            pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue()),
                                            Sort.by("createdAt").descending());

        List<JobPosting> postings= jobService.findPostings(employerName,branch,pageable);
         return postings.stream().map(i->{
            var view= new JobPostingView(i,false);
            BranchView branchView= new BranchView();
            branchView.setBranchName(i.getBranch().getBranchName());
            branchView.setLocation(i.getBranch().getLocation());
            view.setBranch(branchView);
            return view;
        }).toList();
    }

    @Operation(summary = "Appends job posting",
            description = "Privileged access required")
    @PatchMapping(path = "/{id}",consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void modifyPosting(@PathVariable(name = "id")long id,
                       @Valid @RequestBody JsonPatch patch) throws ResourceNotFoundException, IllegalAccessException,JsonPatchException, JsonProcessingException {
        final String methodString=className+"modifyPosting()";
        //TODO:Logging function calls maybe redundant
        logger.trace(LoggerHelper.getMethodCallingString(2),"id",id,"patch",patch);
        EmployerBranch pair= pairService.getPairFromPosting(id);
        jobService.modifyJobPosting(pair,patch,id);
    }

    @Operation(summary = "Deletes job posting",
            description = "Privileged access required")
    @DeleteMapping(path = "/{id}")
    void deletePosting(@PathVariable(name = "id") long id) throws ResourceNotFoundException,IllegalAccessException {
        EmployerBranch pair= pairService.getPairFromPosting(id);
        jobService.removePosting(pair,id);
    }

    // TODO: Check for valid enums

    @Operation(summary = "Sets employer status for match",
            description = "Privileged access required")
    @PostMapping(path = "/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void setApplicationStatus(@RequestParam long candidateID,
                              @RequestParam long postingID,
                              @RequestParam MatchEmployerStatus newStatus) throws AuxiliaryResourceNotFoundException, ResourceNotFoundException,InternalException,IllegalAccessException {

        EmployerBranch pair= null;
        try {
            pair = pairService.getPairFromPosting(postingID);
        } catch (ResourceNotFoundException e) {
            throw new AuxiliaryResourceNotFoundException(e.getMessage());
        }
        empPostingService.setApplicationStatus(pair,candidateID,postingID,newStatus);

    }
}


