package com.example.demo.Repositories;

import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface MatchRepo extends JpaRepository<Match, Match.CompositeKey>, CustomMatchRepo {
    long countByKey_CandidateId(long candidateId);
    List<Match> findByKey_CandidateId(long candidateId, Pageable pageable);

    //TODO: Test if method works
    @Transactional
    long deleteByKey_CandidateId(long candidateId);

    @Transactional
    long deleteByCandidateProfile_ID(long candidateId);

    boolean existsByKey(Match.CompositeKey key);

    //TODO: Order by score
    List<Match> findByKey_CandidateIdAndCandidateStatus(long candidateId, MatchCandidateStatus status, Pageable pageable);
    long countByKey_PostingIdAndEmployerStatus(long postingId, MatchEmployerStatus status);

    List<Match> findByKey_PostingIdAndEmployerStatus(long postingId, MatchEmployerStatus employerStatus,Pageable pageable);

    List<Match> findByPosting_IDAndEmployerStatus(long ID, MatchEmployerStatus employerStatus,Pageable pageable);







    //TODO: Order by score
    List<Match> findByKeyPostingId(long postingId, Pageable pageable);
    @Transactional
    long deleteByPosting_ID(long postingId);

    @Transactional
    long deleteByPosting_Branch_ID(long branchID);

    //TODO: Method doesn't work
    @Transactional
    long deleteByPostingIn(Collection<JobPosting> postingIds);




}
