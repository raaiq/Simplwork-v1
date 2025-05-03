package com.example.demo.Services;

import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.UserInfo;
import com.example.demo.Domain.TypesAndEnums.Compositions.CandidateUser;
import com.example.demo.Exceptions.InternalException;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Exceptions.ResourceAlreadyExistsException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Repositories.*;
import com.example.demo.Services.Other.MapBoxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//TODO:Skew/Generalize location

//Maybe add UUID to access token
//TODO: Support multiple candidate profiles


//Have employer related roles for viewing candidate profiles
@Service
public class CandidateService {

    @Autowired
    private CandidateRepo candidateRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private Validator validator;
    @Autowired
    private MatcherService matcherService;

    @Autowired
    private MatchRepo matchRepo;

    @Autowired
    private MapBoxService mapBoxService;
    @Autowired
    private ObjectMapper objectMapper;

    //TODO:Remove redundant save operation
    @PreAuthorize(value = "hasRole('USER')")
    @Transactional
    public CandidateUser addCandidateProfile(CandidateUser candidateUser) throws ResourceAlreadyExistsException, OperationConditionsFailedException, InternalException {

        var contextUser=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        if(contextUser.hasCandidateProfile()){
            throw new ResourceAlreadyExistsException("Candidate profile already exists for user");
        }

        CandidateProfile candidateProfile=candidateUser.getCandidateProfile();
        UserInfo userInfo= candidateUser.getUser();
        candidateProfile.initializeNewEntity();
        candidateProfile.setLocation(mapBoxService.getMapboxLocation(candidateProfile.getLocation()));
        var savedCandidate= candidateRepo.save(candidateProfile);

        var repoUser= userRepo.findById(contextUser.getID()).get();
        repoUser.addCandidateProfile(savedCandidate);
        repoUser.setAge(userInfo.getAge());
        repoUser.setGender(userInfo.getGender());
        repoUser.setPhoneNumber(userInfo.getPhoneNumber());
        String username= userInfo.getName();
        if(username != null && !username.equals("")){
            repoUser.setName(username);}
        repoUser=userRepo.save(repoUser);

        matcherService.matchProfile(savedCandidate);

        return new CandidateUser(savedCandidate,repoUser);

    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void updateCandidateProfile(JsonPatch patch) throws ResourceNotFoundException, JsonPatchException, JsonProcessingException, OperationConditionsFailedException, InternalException {
        UserInfo user=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        if(!user.hasCandidateProfile()){
            throw  new ResourceNotFoundException("Candidate profile doesn't exits");
        }
        CandidateProfile storedProfile = user.getCandidatesProfiles().get(CandidateProfile.DEFAULT_CANDIDATE_PROFILE);

        CandidateUser storedUser= new CandidateUser(storedProfile,user);
        JsonNode node = patch.apply(objectMapper.convertValue(storedUser,JsonNode.class));

        CandidateUser patchedCandidateUser = objectMapper.treeToValue(node, CandidateUser.class);
        patchedCandidateUser.copyFixedTraitsAndSanitizeFields(storedProfile,user);

        CandidateProfile patchedCandidateProfile=patchedCandidateUser.getCandidateProfile();
        //TODO: Redundant to calculate for rematch if location changed
        if(!patchedCandidateProfile.getLocation().equals(storedProfile.getLocation())){
            patchedCandidateProfile.setLocation(mapBoxService.getMapboxLocation(patchedCandidateProfile.getLocation()));
        }
        Set<ConstraintViolation<CandidateUser>> errors =validator.validate(patchedCandidateUser);
        if(!errors.isEmpty()){
            throw new ConstraintViolationException(errors);
        }
        //Check if new candidate profile is updated in context automatically
        var savedProfile=candidateRepo.save(patchedCandidateProfile);
        userRepo.save(patchedCandidateUser.getUser());
        if(savedProfile.requiresRematching(storedProfile)){
            List<Match> matches;
            //TODO: Rematch algorithm very inefficient
            do{
            matches=matchRepo.findByKey_CandidateId(savedProfile.getID(), PageRequest.of(0,100));
            matches=matches.stream().filter(
                    (match)->!match.isManualMatch() && (match.getCandidateStatus()==MatchCandidateStatus.APPLIED || match.getCandidateStatus()==MatchCandidateStatus.WITHDRAWN)
                    ).toList();
            matchRepo.deleteAllInBatch(matches);
            } while(!matches.isEmpty());
            matcherService.matchProfile(savedProfile);
        }
    }
    @PreAuthorize("hasRole('USER')")
    public CandidateUser getDefaultCandidateProfile() throws ResourceNotFoundException {
        return getCandidateProfileByName(CandidateProfile.DEFAULT_CANDIDATE_PROFILE);
    }

    @PreAuthorize("hasRole('USER')")
    private CandidateUser getCandidateProfileByName(String profileName) throws ResourceNotFoundException {
        var user=((UserInfoInterface)SecurityContextHolder.getContext().getAuthentication()).getUserInfo();
        var candidateProfiles= user.getCandidatesProfiles();
        if(!candidateProfiles.containsKey(profileName)){
            throw new ResourceNotFoundException("Candidate profile doesn't exists");
        }
        return new CandidateUser(candidateProfiles.get(profileName),user);
    }

    //TODO: Remove in production
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void deleteCandidateProfile() {
        CandidateUser profile;

        try {
            profile=getDefaultCandidateProfile();
        } catch (ResourceNotFoundException e) {
            return;
        }

        matchRepo.deleteByCandidateProfile_ID(profile.getCandidateProfile().getID());
        UserInfo user=profile.getUser();
        user.getCandidatesProfiles().clear();


        userRepo.save(user);
        candidateRepo.delete(profile.getCandidateProfile());
    }

}
