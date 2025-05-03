package com.example.demo.Domain.Views;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployerView {
    String companyName;
    String companyDescription;

    UUID ImageID;
    List<BranchView> branches;
}
