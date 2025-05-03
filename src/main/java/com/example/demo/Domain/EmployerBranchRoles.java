package com.example.demo.Domain;

import com.example.demo.Domain.RolesAndAuthorities.BranchRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class EmployerBranchRoles {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    EmployerUser employerUser;

    @ManyToOne
    Branch branch;

    @Column(columnDefinition = "TEXT")
    Set<BranchRole> roles= new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployerBranchRoles that = (EmployerBranchRoles) o;

        if (id != that.id) return false;
        if (!Objects.equals(branch, that.branch)) return false;
        return Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }
}
