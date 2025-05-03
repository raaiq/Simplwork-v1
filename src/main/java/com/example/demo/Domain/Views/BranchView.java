package com.example.demo.Domain.Views;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BranchView {
    public String company;
    public String branchName;
    public Location location;
    public List<JobPostingView> jobs;
    Integer noOfJobs;


    public BranchView(Branch branch){
        branchName=branch.getBranchName();
        location=branch.getLocation();
    }
    public BranchView(){}
}
