package com.example.demo.Domain.TypesAndEnums.Compositions;

import com.example.demo.Domain.Match;
import com.example.demo.Domain.JobPosting;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO:Have better name
@Data
@NoArgsConstructor
public class MatchPostingPair {
    public Match match;
    public JobPosting jobPosting;

    public MatchPostingPair(JobPosting posting, Match match) {
        this.match = match;
        jobPosting = posting;
    }
}
