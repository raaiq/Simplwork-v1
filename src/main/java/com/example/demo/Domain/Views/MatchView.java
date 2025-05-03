package com.example.demo.Domain.Views;

import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

//TODO:Set commute distance for candidatePosting
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchView {
    JobPostingView posting;
    CandidateProfileView candidateProfile;

    MatchEmployerStatus employerStatus;
    MatchCandidateStatus candidateStatus;
    List<ShiftCompatibilityView> shiftCompatibilities;
    Integer walkCommuteTime,bikeCommuteTime,carCommuteTime;
    Double distance;
    Integer matchingHours,shiftScore,potentialEarning, candScore,empScore;

    public MatchView(){}

    public MatchView(Match match,boolean forEmployer){
        distance=match.getDistanceToWork();
        matchingHours=match.getMatchingHours();
        shiftScore= match.getShiftScore();
        employerStatus=match.getEmployerStatus();
        if(forEmployer){
            empScore=match.getEmpScore();
            return;
        }
        potentialEarning=match.getPotentialEarnings();
        candScore=match.getCandScore();
        walkCommuteTime=match.getWalkCommuteTime();
        bikeCommuteTime=match.getBikeCommuteTime();
        carCommuteTime=match.getCarCommuteTime();
        candidateStatus=match.getCandidateStatus();




    }
    }

