package com.example.demo.Repositories;

import com.example.demo.Domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerRepo extends JpaRepository<Employer,Long> {

    boolean existsByCompanyNameAllIgnoreCase(String companyName);

    Optional<Employer> findByCompanyNameIgnoreCase(String companyName);




}
