package com.example.demo.Services;

import com.example.demo.Domain.*;
import com.example.demo.Authentication.UserInfoInterface;
import com.example.demo.Domain.RolesAndAuthorities.BranchAuthority;
import com.example.demo.Domain.RolesAndAuthorities.BranchRole;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Domain.Views.Directors.ViewDirector;
import com.example.demo.Domain.Views.PostingEmployerView;
import com.example.demo.Exceptions.*;
import com.example.demo.Repositories.*;
import com.example.demo.Controllers.EmployerPostingController;
import com.example.demo.Services.Other.ActionTokenService;
import com.example.demo.Services.Other.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployerPostingService {

    @Autowired
    private MatchRepo matchRepo;
    @Autowired
    private BranchRepo  branchRepo;
    @Autowired
    private JobPostingRepo jobPostingRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ActionTokenService tokenService;
    @Autowired
    private UserRepo userRepo;
    private static final String className= EmployerPostingController.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);


    //TODO:Optimize method
    public List<PostingEmployerView> getPostingsOverview(String employerName, Optional<String> branchName, Pageable jobsPageable) throws AuxiliaryResourceNotFoundException, IllegalAccessException {

        Map<Branch,Set<BranchRole>> userBranchRoles;
        Optional<Branch> targetBranch;
        try {
            Authentication auth= SecurityContextHolder.getContext().getAuthentication();
            EmployerUser empUser= ((UserInfoInterface)auth).getUserInfo().findEmployerUser(employerName);
            userBranchRoles=empUser.getBranchRolesMap();
        } catch (ResourceNotFoundException e) {
            throw new IllegalAccessException("Invalid code path: Employer not associated with user");
        }
        List<Branch> branches;

        if(branchName.isEmpty()){
            branches=userBranchRoles.entrySet().stream()
                                        .filter(e->e.getValue().stream().anyMatch(i->i.hasAuthority(BranchAuthority.POST_READ)))
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.toList());

        }else if ((targetBranch=branchRepo.findByCompany_CompanyNameAndBranchName(employerName,branchName.get())).isPresent()
                    && userBranchRoles.containsKey(targetBranch)
                    && userBranchRoles.get(targetBranch).stream().anyMatch(i->i.hasAuthority(BranchAuthority.POST_READ))){
            branches= List.of(targetBranch.get());
        }else{
            throw new AuxiliaryResourceNotFoundException("Branch "+ branchName+" for employer "+ employerName);
        }
        List<String> branchNames=branches.stream().map(Branch::getBranchName).toList();
        List<JobPosting> postings=jobPostingRepo.findByBranch_BranchNameInAndBranch_Company_CompanyName(branchNames,employerName,jobsPageable);

        return postings.stream().map(this::getPostingView).collect(Collectors.toList());

    }


    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_READ)")
    public PostingEmployerView getPostingOverview(EmployerBranch pair, long postingID) throws ResourceNotFoundException, IllegalAccessException {
        JobPosting posting= jobPostingRepo.findJobPostingByID(postingID).orElseThrow(()->new ResourceNotFoundException("Job Posting with ID: "+postingID+" not found"));
        if(posting.getBranch().getID() != pair.getBranch().getID()){
            throw new IllegalAccessException("Attempt to access job posting from different branch and/or employer");
        }
        return getPostingView(posting);
    }

    private PostingEmployerView getPostingView(JobPosting posting){
        PostingEmployerView view= new PostingEmployerView();
        view.jobPosting= ViewDirector.getJobPostingEmployerView(posting);
        //TODO: Query counts for all statuses at once
        view.reviewed_count = matchRepo.countByKey_PostingIdAndEmployerStatus(posting.getID(), MatchEmployerStatus.REVIEWED);
        view.new_count = matchRepo.countByKey_PostingIdAndEmployerStatus(posting.getID(), MatchEmployerStatus.NEW);
        view.interview_requested_count = matchRepo.countByKey_PostingIdAndEmployerStatus(posting.getID(), MatchEmployerStatus.INTERVIEW_REQUESTED);
        view.ready_for_interview_count= matchRepo.countByKey_PostingIdAndEmployerStatus(posting.getID(),MatchEmployerStatus.READY_FOR_INTERVIEW);
        view.rejected_count = matchRepo.countByKey_PostingIdAndEmployerStatus(posting.getID(), MatchEmployerStatus.REJECTED);
        return view;
    }
    //TODO: Use post authorize if possible
    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_READ)")
    public List<Match> getMatches(EmployerBranch pair, long postingID,MatchEmployerStatus status,Pageable pageable) throws IllegalAccessException{

       List<Match> matches=matchRepo.findByKey_PostingIdAndEmployerStatus(postingID,status,pageable);
       //TODO: Maybe can remove redundant check
       if(!matches.isEmpty() && matches.get(0).getPosting().getBranch().getID() != pair.getBranch().getID()){
           throw new IllegalAccessException("Attempt to access job matches from different branch and/or employer");
       }
       return matches;

    }

    //TODO: Use post authorize if possible
    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_READ)")
    public Match getMatch(EmployerBranch pair, long candidateID, long postingID) throws ResourceNotFoundException, IllegalAccessException {
        Match.CompositeKey key= new Match.CompositeKey(candidateID,postingID);
        Optional<Match> candidatePosting= matchRepo.findById(key);
        Match match=candidatePosting.orElseThrow(()->new ResourceNotFoundException("Unable to find job match"));
        if(match.getPosting().getBranch().getID() !=pair.getBranch().getID()){

            //TODO: Might be redundant
            throw new IllegalAccessException("Attempt to access job match from different branch and/or employer");
        }
        return match;
    }

    @PreAuthorize("hasPermission(#pair,T(com.example.demo.Domain.RolesAndAuthorities.BranchAuthority).POST_MODIFY)")
    @Transactional
    public void setApplicationStatus(EmployerBranch pair, long candidateID, long postingID, MatchEmployerStatus status) throws ResourceNotFoundException, IllegalAccessException, InternalException {
        Match cp= getMatch(pair,candidateID,postingID);
        cp.setEmployerStatus(status);
        if(status== MatchEmployerStatus.INTERVIEW_REQUESTED){

            //TODO:Allow candidate to refer to user without errors
            UserInfo user=userRepo.findByCandidatesProfiles_ID(candidateID).orElseThrow(()->
                new InternalException("Couldn't find candidate profile for match "+candidateID)
            );
            cp.setCandidateStatus(MatchCandidateStatus.INTERVIEW_OFFER);
            emailService.sendJobInterviewCandidateEmail(cp,user,pair.getEmployer().getCompanyName());
        }
        matchRepo.save(cp);

    }



}
