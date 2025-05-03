package com.example.demo.Repositories;

import com.example.demo.Domain.Employer;
import com.example.demo.Domain.EmployerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EmployerUserRepo extends JpaRepository<EmployerUser, EmployerUser.EmployerUserKey> {
    @Transactional
    long deleteByEmployer(Employer employer);

    List<EmployerUser> findByEmployer_ID(long ID);



}
