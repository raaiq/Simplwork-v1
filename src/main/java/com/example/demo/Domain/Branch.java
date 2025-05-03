package com.example.demo.Domain;

import com.example.demo.Domain.TypesAndEnums.Location;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

//TODO:Properly test hasCode and equals method
//TODO: Test validation works with patch requests
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@NoArgsConstructor
public class Branch {

    @JsonIgnore
    @Id
    @GeneratedValue
    long ID;

    @NotBlank
    @NonNull
    @SafeString
    String branchName;

    @ManyToOne(fetch = FetchType.EAGER)
    Employer company;

    @Embedded
    @NotNull
    @NonNull
    @Valid
    Location location;


    public void copyFixedTraitsAndSanitizeFields(Branch sourceBranch){
        this.setID(sourceBranch.ID);
        this.setCompany(sourceBranch.company);
    }

    public void initializeNewEntity(Employer employer){
        ID=0;
        company=employer;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Branch branch = (Branch) o;

        if (ID != branch.ID) return false;
        if (company.ID != branch.company.ID) return false;
        if (!branchName.equals(branch.branchName)) return false;
        return  location.equals(branch.location);
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + branchName.hashCode();
        result = 31 * result + (int) (company.ID ^ (company.ID >>> 32));
        result = 31 * result + location.hashCode();
        return result;
    }



}
