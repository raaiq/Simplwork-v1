package com.example.demo.Services;

import com.example.demo.Domain.*;
import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchSortBy;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Exceptions.AuxiliaryResourceNotFoundException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Repositories.EmployerUserRepo;
import com.example.demo.Repositories.JobPostingRepo;
import com.example.demo.Repositories.MatchRepo;
import com.example.demo.Repositories.UserRepo;
import com.example.demo.Services.Other.EmailService;
import com.example.demo.Structures.Sanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CandidatePostingService {

    @Autowired
    private MatchRepo matchRepo;

    @Autowired
    private MatcherService matcherService;

    @Autowired
    private JobPostingRepo jobPostingRepo;

    @Autowired
    private EmployerUserRepo employerUserRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepo userRepo;

    private static final Logger logger= LoggerFactory.getLogger(CandidatePostingService.class);

    //TODO: Return Match instead of CandidateJobPostingPair
    //TODO: Allow searching by jobID
    @PreAuthorize("hasRole('USER')")
    public List<MatchPostingPair> getJobList(String queryString, Pageable pageable) throws AuxiliaryResourceNotFoundException {

        CandidateProfile profile= null;
        try {
            profile = getUserProfile();
        } catch (ResourceNotFoundException e) {
            throw new AuxiliaryResourceNotFoundException(e);
        }
        Location.LocationBounds userBoundaries = profile.getLocationBoundaries();
        queryString = Sanitizer.sanitize(queryString, "queryString");
        EscapeCharacter escapeCharacter=EscapeCharacter.of('_');
        queryString= escapeCharacter.escape(queryString);
        queryString= EscapeCharacter.of('%').escape(queryString);
        queryString= "%"+queryString+"%";
        return matchRepo.searchForPostings(userBoundaries.latitudeLow,userBoundaries.latitudeHigh,
                                                     userBoundaries.longitudeLow,userBoundaries.longitudeHigh,
                                                     profile.getID(),queryString,pageable);

    }

    @PreAuthorize("hasRole('USER')")
    public List<Match> getJobListByStatus(Set<MatchCandidateStatus> requiredStatus, Pageable pageable) throws AuxiliaryResourceNotFoundException {

        List<Match> list=new ArrayList<>();
        for (MatchCandidateStatus status:requiredStatus
        ) {
            try {
                list.addAll(matchRepo.findByKey_CandidateIdAndCandidateStatus(getUserProfile().getID(),status,pageable));
            } catch (ResourceNotFoundException e) {
                throw new AuxiliaryResourceNotFoundException(e);
            }
        }
        return list;
    }

    @PreAuthorize("hasRole('USER')")
    public Match getMatchByJobID(long postingID) throws AuxiliaryResourceNotFoundException,ResourceNotFoundException {
        long candidateID;
        try {
            candidateID=getUserProfile().getID();
        }catch (ResourceNotFoundException e){
            throw new AuxiliaryResourceNotFoundException(e);
        }
        Match.CompositeKey key= new Match.CompositeKey(candidateID,postingID);
        Optional<Match> candidatePosting= matchRepo.findById(key);
        return candidatePosting.orElseThrow(()->new ResourceNotFoundException("Unable to find match with posting id "+postingID));
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public Match setApplicationStatus(long postingID, MatchCandidateStatus status) throws ResourceNotFoundException, OperationConditionsFailedException, AuxiliaryResourceNotFoundException {
        Match match=null;
        try {
            match= getMatchByJobID(postingID);
            match.setCandidateStatus(status);
            if(status == MatchCandidateStatus.ACCEPT_INTERVIEW) {
                match.setEmployerStatus(MatchEmployerStatus.READY_FOR_INTERVIEW);

                Stream<String> employerUserEmails=employerUserRepo.findByEmployer_ID(match.getPosting().getBranch().getCompany().getID()).stream().map((element)->element.getUserInfo().getEmail());
                UserInfo candidateUser= userRepo.findByCandidatesProfiles_ID(match.getCandidateProfile().getID()).get();

                Match finalMatch = match;
                employerUserEmails.forEach((email)->{emailService.sendInterviewEmployerEmail(finalMatch, candidateUser.getName(), email);});
            }
            //TODO: Better handle withdrawn applications
//            else if(status == MatchCandidateStatus.WITHDRAWN){
//                matchRepo.deleteById(match.getKey());
//                return null;
//            }
            match=matchRepo.save(match);
        }catch (ResourceNotFoundException e){
            if(status==MatchCandidateStatus.APPLIED){
            Optional<JobPosting> entry=jobPostingRepo.findJobPostingByID(postingID);
            JobPosting posting= entry.orElseThrow(()->new ResourceNotFoundException("Job with ID: "+postingID+" doesn't exist"));
            Optional<Match> newMatch=matcherService.match(getUserProfile(),posting,true);
            match= matchRepo.save(newMatch.get());
            }
        }

        return match;
    }


    private CandidateProfile getUserProfile() throws ResourceNotFoundException {
        UserInfo userInfo = ((UserInfoInterface) SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        CandidateProfile profile= userInfo.getCandidatesProfiles().get(CandidateProfile.DEFAULT_CANDIDATE_PROFILE);
        if (profile==null){
            throw new ResourceNotFoundException("Candidate profile doesn't exist");
        }
        return profile;
    }


}
