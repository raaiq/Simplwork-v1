package com.example.demo.Repositories;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.TypesAndEnums.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepo extends JpaRepository<JobPosting,Long> {

    Optional<JobPosting> findJobPostingByID(long ID);

    List<JobPosting> findByBranch_ID(long branchID,Pageable pageable);

    boolean existsByIDAndBranch_ID(long ID, long branchID);


    @Transactional
    long deleteByBranch(Branch branch);

    List<JobPosting> findByBranch_Location_LatitudeBetweenAndBranch_Location_LongitudeBetween(double latitudeStart, double latitudeEnd, double longitudeStart, double longitudeEnd);

    List<JobPosting> findByBranch_Company_CompanyName(String companyName, Pageable pageable);

    List<JobPosting> findByBranch_BranchNameInAndBranch_Company_CompanyName(Collection<String> branchNames, String companyName,Pageable pageable);






    List<JobPosting> findByBranch_IDIn(Collection<Long> IDS, Pageable pageable);





}
