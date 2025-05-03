package com.example.demo.Domain.TypesAndEnums.Compositions;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Employer;
import com.example.demo.Repositories.BranchRepo;
import com.example.demo.Repositories.EmployerRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

//TODO:Have a cleaner way to do this
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerBranch {
    private Employer employer = null;
    private Branch branch = null;
}
