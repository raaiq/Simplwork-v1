package com.example.demo.Controllers;

import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchSortBy;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Views.MatchView;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Exceptions.AuxiliaryResourceNotFoundException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Services.CandidatePostingService;
import com.example.demo.Services.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Candidate Job Search", description = "For viewing and applying to jobs and matches")
@RestController
@Validated
@RequestMapping(path = "api/candidate/postings",produces = "application/json")
public class CandidatePostingController {

    private static final String className= CandidatePostingController.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);
    @Autowired
    private CandidatePostingService candidatePostingService;

    @Autowired
    private CandidateService candidateService;
    //TODO: Test for invalid query parameters
    //TODO: Have better way to document default page size
    //TODO: Important. Have matches come before just postings
    //TODO: Have filter for only matches or postings
    @Operation(summary = "Searches for jobs near user's location")
    @GetMapping(path = "/search")
    List<MatchView> searchJobPostings(@Parameter(description = "Can be job title or company name") @RequestParam Optional<@SafeString String> queryString,
                                      @Parameter(description = "Sort results by match or job properties defaults to POTENTIAL_EARNINGS") @RequestParam() Optional<MatchSortBy> sortBy,
                                      @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(1) Integer> pageSize,
                                      @RequestParam(defaultValue = "0") @Min(0) Integer pageNo,
                                      @Parameter(description = "Sort by direction, defaults to descending")@RequestParam Optional<Sort.Direction> direction) throws AuxiliaryResourceNotFoundException, ResourceNotFoundException {

        int pageSizeInt= pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue());
        Sort.Order order=new Sort.Order(direction.orElse(Sort.Direction.DESC),sortBy.orElse(MatchSortBy.POTENTIAL_EARNINGS).JPAAnalogCompositeAnalog, Sort.NullHandling.NULLS_LAST);
        Pageable pageable=PageRequest.of(pageNo,pageSizeInt,Sort.by(order));

        List<MatchPostingPair> list = candidatePostingService.getJobList(queryString.orElse(""),pageable);

        //TODO: Remove redundant error
        return ViewDirector.getMatchCandidateView(list,candidateService.getDefaultCandidateProfile().getCandidateProfile());

    }

    //TODO: Use Contants.DEFAULT_PAGE_SIZE for default page size using spEL

    @Operation(summary = "Gets job matches for user for a set of match statuses or a job id. If neither are set then all matches are returned for the user")
    @GetMapping(path = "/personal")
    List<MatchView> getPostingInfo(@Parameter(description = "Required status for the match")@RequestParam(required = false) List<MatchCandidateStatus> requestStatusSet,
                                   @Parameter(description = "Job id related to match. Takes precedence over 'requiredStatusSet' if both params are set.")@RequestParam Optional<Long> jobID,
                                   @Parameter(schema = @Schema(defaultValue = "20")) @RequestParam Optional<@Min(1) Integer> pageSize,
                                   @RequestParam(defaultValue = "0") @Min(0) Integer pageNo) throws AuxiliaryResourceNotFoundException,ResourceNotFoundException {


        List<MatchView> matchViews = null;


        if(jobID.isPresent()){
            matchViews= List.of(ViewDirector.getMatchCandidateView(candidatePostingService.getMatchByJobID(jobID.get())));
        } else {

            Set<MatchCandidateStatus> statusSet= requestStatusSet == null ?  new HashSet<>(Arrays.asList(MatchCandidateStatus.values())) : new HashSet<>(requestStatusSet);

            Pageable pageable=PageRequest.of(   pageNo,
                                                pageSize.orElse(Constants.DEFAULT_PAGE_SIZE.getIntValue()),
                                                Sort.by("potentialEarnings").descending().and(Sort.by("distanceToWork").ascending()));

            List<Match> matchList = candidatePostingService.getJobListByStatus(statusSet,pageable);
            matchViews = matchList.stream().map(ViewDirector::getMatchCandidateView).collect(Collectors.toList());
        }

        return matchViews;

    }

    @Operation(summary = "Sets candidate status for match")
    @PostMapping(path = "/setStatus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setApplicationStatus(@Parameter(description = "Posting ID associated with job match")@RequestParam(name = "id") Long postingID,
                              @Parameter(description = "Status to set to")@RequestParam MatchCandidateStatus status  ) throws OperationConditionsFailedException,AuxiliaryResourceNotFoundException,ResourceNotFoundException {
        candidatePostingService.setApplicationStatus(postingID, status);

    }


}
