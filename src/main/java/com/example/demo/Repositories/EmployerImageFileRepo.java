package com.example.demo.Repositories;

import com.example.demo.Domain.EmployerImageFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployerImageFileRepo extends CrudRepository<EmployerImageFile, UUID> {
}
