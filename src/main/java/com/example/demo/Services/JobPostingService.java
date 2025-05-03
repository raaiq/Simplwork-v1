package com.example.demo.Services;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Repositories.MatchRepo;
import com.example.demo.Repositories.JobPostingRepo;
import com.example.demo.Repositories.ShiftRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//TODO: Maybe combine employer posting and job service
@Service
public class JobPostingService {

    @Autowired
    private MatchRepo matchRepo;

    @Autowired
    private ShiftRepo shiftRepo;
    @Autowired
    private JobPostingRepo jobPostingRepo;
    @Autowired
    private Validator validator;
    @Autowired
    private MatcherService matcherService;
    @Autowired
    private ObjectMapper objectMapper;




    //TODO: Have better way to save unique shifts
    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_CREATE)")
    @Transactional
    public JobPosting savePosting(EmployerBranch pair, JobPosting posting) {
        posting.initializeNewEntity(pair.getBranch());
        List<Shift> savedShifts=saveShifts(posting.getShifts(),posting.getBranch());
        posting.setShifts(savedShifts);
        JobPosting savedPosting=jobPostingRepo.save(posting);
        matcherService.matchJobPosting(savedPosting);
        return savedPosting;
    }

    /*Finds only the job posting, not candidates*/
    @PreAuthorize("hasRole('USER')")
    public JobPosting findPosting(long postingId) throws ResourceNotFoundException {
        return jobPostingRepo.findById(postingId).orElseThrow(()->new ResourceNotFoundException("Couldn't find job posting"));
    }

    @PreAuthorize("hasRole('USER')")
    public List<JobPosting> findPostings(String employer, Optional<String> branch, Pageable pageable){
        if(branch.isPresent()){
            return jobPostingRepo.findByBranch_BranchNameInAndBranch_Company_CompanyName(List.of(branch.get()),employer,pageable);
        }
        return jobPostingRepo.findByBranch_Company_CompanyName(employer,pageable);

    }

    //Handle case where pay is below minimum
    //TODO: Rematch algorithm very inefficient
    //TODO: Use Post Authorize if need be
    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_MODIFY)")
    @Transactional
    public void modifyJobPosting(EmployerBranch pair, JsonPatch patch, long jobID) throws JsonPatchException, JsonProcessingException,ResourceNotFoundException,IllegalAccessException {

        JobPosting oldPosting= jobPostingRepo.findJobPostingByID(jobID).orElseThrow(()->new ResourceNotFoundException("Couldn't find job posting"));
        if(pair.getBranch().getID() != oldPosting.getBranch().getID()){
            throw new IllegalAccessException("Job posting not accessible to user");
        }
        JsonNode node = patch.apply(objectMapper.convertValue(oldPosting,JsonNode.class));
        JobPosting patchedPosting = objectMapper.treeToValue(node, JobPosting.class);
        patchedPosting.copyFixedTraitsAndSanitizeFields(oldPosting);
        Set<ConstraintViolation<JobPosting>> errors =validator.validate(patchedPosting);
        if(!errors.isEmpty()){
            throw new ConstraintViolationException(errors);
        }
        boolean rematch=false;
        try {
            rematch=patchedPosting.requiresRematching(oldPosting);
        }catch (OperationConditionsFailedException e){
            throw new RuntimeException(e);
        }

        patchedPosting.setShifts(saveShifts(patchedPosting.getShifts(),oldPosting.getBranch()));
        var savedPosting=jobPostingRepo.save(patchedPosting);
        if(rematch){
           List<Match> matches;
           do {
               matches = matchRepo.findByKeyPostingId(oldPosting.getID(), PageRequest.of(0, 100));
               matches = matches.stream().filter((match) -> match.getEmployerStatus() == MatchEmployerStatus.NEW || match.getEmployerStatus() == MatchEmployerStatus.REVIEWED).toList();
               matchRepo.deleteAllInBatch(matches);
           }while (!matches.isEmpty());
            matcherService.matchJobPosting(savedPosting);
        }

    }

    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_DELETE)")
    @Transactional
    public void removePosting(EmployerBranch pair, long postingID) throws IllegalAccessException {
        //TODO: Redundant method
        if(!jobPostingRepo.existsByIDAndBranch_ID(postingID,pair.getBranch().getID())){
            throw new IllegalAccessException("Job posting not accessible to user");
        }
        matchRepo.deleteByPosting_ID(postingID);
        jobPostingRepo.deleteById(postingID);

    }

    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_DELETE)")
    @Transactional
    public void removeAllPostingsInBranch(EmployerBranch pair){
        matchRepo.deleteByPosting_Branch_ID(pair.getBranch().getID());
        jobPostingRepo.deleteByBranch(pair.getBranch());
    }


    @Transactional
    private List<Shift> saveShifts(List<Shift> list, Branch branch){
        return list.stream().map((i)->{i.setBranch(branch);
                return shiftRepo.findByDayOfWeekAndShiftTimes_StartTimeAndShiftTimes_EndTimeAndBranch_ID(i.getDayOfWeek(),i.getShiftTimes().startTime, i.getShiftTimes().endTime,i.getBranch().getID())
                        .orElseGet(()->shiftRepo.save(i));}
        ).toList();
    }

}
