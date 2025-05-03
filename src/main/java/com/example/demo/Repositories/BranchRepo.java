package com.example.demo.Repositories;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Employer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepo extends JpaRepository<Branch,Long> {
    boolean existsByCompanyIDAndBranchName(long companyID, String branchName);

    long deleteByBranchName(String branchName);
    Optional<Branch> findByCompanyIDAndBranchName(long companyID, String branchName);

    long countByCompanyID(long companyID);

    List<Branch> findByCompany_CompanyName(String companyName, Pageable pageable);

    List<Branch> findByCompany_ID(long ID, Pageable pageable);

    long deleteByCompany(Employer company);

    Optional<Branch> findByCompany_CompanyNameAndBranchName(String companyName, String branchName);

    List<Branch> findByCompany_CompanyNameAndBranchNameIn(String companyName, Collection<String> branchNames);





}
