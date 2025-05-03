package com.example.demo.Domain;

import com.example.demo.Domain.RolesAndAuthorities.BranchRole;
import com.example.demo.Domain.RolesAndAuthorities.EmployerRole;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Bug in liquibase schema generation bug where map of branch roles expects employer and user columns in branch entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(EmployerUser.EmployerUserKey.class)
public class EmployerUser {

    @Id
    @ManyToOne
    private Employer employer;

    @Id
    @ManyToOne
    private UserInfo userInfo;


    @OneToMany(fetch = FetchType.EAGER,orphanRemoval = true,mappedBy = "employerUser")
    private List<EmployerBranchRoles> branchRoles= new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<EmployerRole> roles=new HashSet<>();

    @Transient
    private Map<Branch,Set<BranchRole>> branchRolesMap;

    public void addRoles(EmployerRole ... roles){
        this.roles.addAll(Arrays.stream(roles).collect(Collectors.toSet()));

    }

    //TODO: Inefficient way to add branch roles
    public void addBranchRoles(Branch branch, BranchRole ... roles){
        Set<BranchRole> roleSet= Arrays.stream(roles).collect(Collectors.toSet());
//        Set<BranchRole> branchRoleSet=branchRolesMap.get(branch);
//        if(branchRoleSet == null){
//            branchRolesMap.put(branch,roleSet);
//        }else{
//            branchRoleSet.addAll(roleSet);
//        }
        List<EmployerBranchRoles> branchRole= getBranchRoles().stream().filter(i->i.branch==branch).toList();
        EmployerBranchRoles roles1;
        if(branchRole.isEmpty()){
            roles1=new EmployerBranchRoles();
            roles1.roles=roleSet;
            roles1.branch=branch;
            roles1.employerUser=this;
            getBranchRoles().add(roles1);

        }else {
            roles1=branchRole.get(0);
            roles1.roles.addAll(roleSet);
        }
    }

    public Map<Branch,Set<BranchRole>> getBranchRolesMap(){
        if(branchRolesMap ==null){
            branchRolesMap = new HashMap<>();
            branchRoles.forEach(i-> branchRolesMap.put(i.branch,i.roles));
        }
        return branchRolesMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployerUser that = (EmployerUser) o;

        if (!employer.equals(that.employer)) return false;
        if (!userInfo.getID().equals(that.userInfo.getID())) return false;
        if (!Objects.equals(getBranchRoles(), that.
                getBranchRoles())) return false;
        return Objects.equals(
                getRoles(), that.
                getRoles());
    }

    @Override
    public int hashCode() {
        int result = employer.hashCode();
        result = 31 * result + userInfo.getID().hashCode();
        result = 31 * result + (getBranchRoles() != null ? getBranchRoles().hashCode() : 0);
        result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
        return result;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployerUserKey implements Serializable {
        Employer employer;
        UserInfo userInfo;
    }
}
