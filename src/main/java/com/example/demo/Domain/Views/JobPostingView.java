package com.example.demo.Domain.Views;

import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.TypesAndEnums.Enums.IndustryType;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobPostingView {
    Long ID;
    List<ShiftView> shifts;
    Boolean isFixedSchedule;
    Integer estimatedHours;
    Double pay;
    IndustryType industryType;
    String positionTitle;
    String jobDescription;
    String benefits;

    ZonedDateTime createdAt;
    EmployerView employer;
    BranchView branch;

    public JobPostingView(){}
    public JobPostingView(JobPosting posting,boolean verbose){
        ID=posting.getID();
        positionTitle=posting.getPositionTitle();
        createdAt= posting.getCreatedAt();
        pay= posting.getPay();
        if(!verbose){
            return;
        }
        jobDescription=posting.getJobDescription();
        benefits= posting.getBenefits();
        isFixedSchedule=posting.getFixedSchedule();
        if(!isFixedSchedule){
            estimatedHours=posting.getEstimatedHours();
        }
    }
}
