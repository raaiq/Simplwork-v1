package com.example.demo.Services.Other;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.TypesAndEnums.Compositions.EmployerBranch;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Repositories.BranchRepo;
import com.example.demo.Repositories.EmployerRepo;
import com.example.demo.Repositories.JobPostingRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;
//TODO: Refactor to remove service
@Service
public class EmployerBranchPairService {

    private final BranchRepo branchRepo;
    private final EmployerRepo employerRepo;
    private final JobPostingRepo jobPostingRepo ;

    public EmployerBranchPairService(BranchRepo branchRepo, EmployerRepo employerRepo, JobPostingRepo jobPostingRepo) {
        this.branchRepo = branchRepo;
        this.employerRepo = employerRepo;
        this.jobPostingRepo = jobPostingRepo;
    }

    public EmployerBranch getPair(String employerName, String branchName) throws ResourceNotFoundException {
        EmployerBranch pair = new EmployerBranch();

        Optional<Branch> branchEntry = branchRepo.findByCompany_CompanyNameAndBranchName(employerName, branchName);
        if (branchEntry.isEmpty()) {
            throw new ResourceNotFoundException("Couldn't find resource for branch name: {"+branchName+"} and employer name: {"+employerName+"}");
        }
        pair.setBranch(branchEntry.get());
        pair.setEmployer(pair.getBranch().getCompany());
        return pair;
    }

    public EmployerBranch getPairFromBranch(long branchID) throws ResourceNotFoundException{
        EmployerBranch pair = new EmployerBranch();
        Optional<Branch> bEntry = branchRepo.findById(branchID);
        if (bEntry.isEmpty()) {
            throw new ResourceNotFoundException("Couldn't find resource from branch ID: "+branchID);
        }
        pair.setBranch(bEntry.get());
        pair.setEmployer(bEntry.get().getCompany());
        return pair;
    }

    public EmployerBranch getPairFromPosting(long postingID) throws ResourceNotFoundException{

        Optional<JobPosting> pEntry=jobPostingRepo.findJobPostingByID(postingID);
        if(pEntry.isEmpty()){
            throw new ResourceNotFoundException("Couldn't find resource from posting ID: "+postingID);
        }
        return new EmployerBranch(pEntry.get().getBranch().getCompany(),pEntry.get().getBranch());
    }
}
