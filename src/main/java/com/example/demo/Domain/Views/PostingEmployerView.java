package com.example.demo.Domain.Views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostingEmployerView {
    public JobPostingView jobPosting;
    public Long new_count, reviewed_count, interview_requested_count, ready_for_interview_count, rejected_count;

    List<CandidateProfileView> new_candidates, reviewed,interview_requested,ready_for_interview,rejected;
}
