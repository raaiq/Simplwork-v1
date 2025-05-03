package com.example.demo.Repositories;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.EmployerBranchRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface EmployerBranchRoleRepo extends JpaRepository<EmployerBranchRoles,Long> {
    @Transactional
    long deleteByBranch_ID(long ID);

    @Transactional
    long deleteByBranch(Branch branch);



}
