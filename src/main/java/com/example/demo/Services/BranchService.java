package com.example.demo.Services;

import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Employer;
import com.example.demo.Domain.EmployerBranchRoles;
import com.example.demo.Domain.EmployerUser;
import com.example.demo.Domain.RolesAndAuthorities.BranchRole;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.example.demo.Exceptions.*;
import com.example.demo.Exceptions.OperationConditionsFailedException;
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

import java.util.*;

//TODO: Make document for endpoints criteria for each HTTP method e.g allowed inputs, etc
//TODO:Move validation and sanitization to services
//TODO:Change RelationNotFoundException to NoSuchElementException
@Service
public class BranchService {


    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private EmployerRepo employerRepo;

    @Autowired
    private JobPostingRepo jobRepo;
    @Autowired
    private EmployerUserRepo employerUserRepo;

    @Autowired
    private EmployerBranchRoleRepo employerBranchRoleRepo;
    @Autowired
    private Validator validator;

    @Autowired
    private ShiftRepo shiftRepo;

    @Autowired
    private JobPostingService jobService;

    @Autowired
    private MapBoxService mapBoxService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String className= BranchService.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);

    @PreAuthorize("hasPermission(#employerName,T(com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority).CREATE_BRANCH)")
    @Transactional
    public Branch createBranch(String employerName,Branch branch) throws InternalException, ResourceAlreadyExistsException, OperationConditionsFailedException {
        EmployerUser employerUser=null;
        try {
            employerUser = ((UserInfoInterface) SecurityContextHolder.getContext().getAuthentication()).getUserInfo().findEmployerUser(employerName);
        }catch (ResourceNotFoundException e){
            throw new InternalException("Authorization shouldn't fail",e);
        }

        Employer employer= employerUser.getEmployer();
        if(branch.getBranchName().isBlank()){
            branch.setBranchName("Branch "+branchRepo.countByCompanyID(employer.getID()));
        } else if( branchRepo.existsByCompanyIDAndBranchName(employer.getID(), branch.getBranchName())){
            throw new ResourceAlreadyExistsException("Branch name "+branch.getBranchName()+ " already exists");
        }
        branch.initializeNewEntity(employer);
        branch.setLocation(mapBoxService.getMapboxLocation(branch.getLocation()));
        Branch savedBranch= branchRepo.save(branch);

        EmployerBranchRoles branchRoles= new EmployerBranchRoles();
        branchRoles.setBranch(savedBranch);
        branchRoles.getRoles().add(BranchRole.SUPER_OWNER);
        branchRoles.setEmployerUser(employerUser);
        employerBranchRoleRepo.save(branchRoles);

        employerUserRepo.save(employerUser);
        return savedBranch;
    }

    //TODO:Have proper way to validate and sanitize json patch
    //TODO: Prevent change of branch location
    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).MODIFY_INFO)")
    @Transactional
    public void modifyBranch(EmployerBranch pair, JsonPatch patch) throws JsonPatchException, JsonProcessingException, OperationConditionsFailedException, InternalException {
        Employer employer=pair.getEmployer();
        Branch originalBranch= pair.getBranch();
        JsonNode node = patch.apply(objectMapper.convertValue(originalBranch,JsonNode.class));
        Branch patchedBranch = objectMapper.treeToValue(node, Branch.class);
        String patchedBranchName=patchedBranch.getBranchName();

        Set<ConstraintViolation<Branch>> errors =validator.validate(patchedBranch);
        if(!errors.isEmpty()){
            throw new ConstraintViolationException(errors);
        }

        if((!patchedBranchName.equals(originalBranch.getBranchName())) && branchRepo.existsByCompanyIDAndBranchName(employer.getID(),patchedBranchName)){
            throw new OperationConditionsFailedException("Cannot modify branch name to "+ patchedBranchName + " as it already exists");
        }
        patchedBranch.copyFixedTraitsAndSanitizeFields(originalBranch);
        Location patchedLocation=patchedBranch.getLocation();
        if(!patchedLocation.equals(originalBranch.getLocation())){
            patchedBranch.setLocation(mapBoxService.getMapboxLocation(patchedLocation));
        }
        branchRepo.save(patchedBranch);
    }
    @PreAuthorize("hasRole('USER')")
    public List<Branch> getBranches(String company, Optional<String> branchName, Pageable pageable) throws ResourceNotFoundException {
       if(branchName.isEmpty() ){
           return branchRepo.findByCompany_CompanyName(company,pageable);
       }
       return List.of(branchRepo.findByCompany_CompanyNameAndBranchName(company,branchName.get()).
                      orElseThrow(()->
                      new ResourceNotFoundException("Couldn't find branch with name: {"+ branchName.get()+ "} for employer: {" + company+"}")));
    }

    //TODO: Accept employer objects for permission evaluations
    //TODO: Method doesn't work when postings already exist
    //TODO: Throw better error
    @PreAuthorize("hasPermission(#pair.getEmployer().getCompanyName(),T(com.example.demo.Domain.RolesAndAuthorities.EmployerAuthority).DELETE_BRANCH)")
    @Transactional
    public void deleteBranch(EmployerBranch pair,boolean forced) throws OperationConditionsFailedException {
        if(!forced && !jobRepo.findByBranch_ID(pair.getBranch().getID(), PageRequest.of(0,100)).isEmpty()){
            throw new OperationConditionsFailedException("Cannot delete branch with active job postings");
        }
        jobService.removeAllPostingsInBranch(pair);
        shiftRepo.deleteByBranch(pair.getBranch());
        employerBranchRoleRepo.deleteByBranch_ID(pair.getBranch().getID());
        branchRepo.delete(pair.getBranch());
    }
}
