package com.example.demo.Domain;

import com.example.demo.Domain.TypesAndEnums.Enums.Gender;
import com.example.demo.Domain.TypesAndEnums.MapBoxUsage;
import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Domain.Validators.Annotations.ValidPhoneNumber;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;


//TODO:Allow lazily initialization
//TODO:Make helper method for map hashcode
//TODO: Check domain for required constraints
@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class UserInfo {

    @JsonIgnore
    @Id
    @GeneratedValue
    private UUID ID;

    @Schema(example = "example@email.com")
    @Email
    @NonNull
    private String email;

    @Min(value = 14, message = "Minimum age must be 14")
    private Short age=14;

    //TODO:Validate field
    private Gender gender;


    @ValidPhoneNumber
    @Schema(example = "293-342-1453")
    private String phoneNumber;


    @SafeString
    @NotNull
    @NonNull
    private String name;

    @JsonIgnore
    @NonNull
    private String OAuth_ID;

    @JsonIgnore
    @Embedded
    private MapBoxUsage geocodingUsage=new MapBoxUsage();

    //TODO: Might be possible to save user and profile at same time
    @JsonIgnore
    @OneToMany(cascade = {CascadeType.REFRESH,CascadeType.REMOVE}, fetch = FetchType.EAGER,orphanRemoval = true)
    @JoinTable(name= "User_Candidate_Profiles_Mapping",joinColumns = {@JoinColumn(name = "User_ID",referencedColumnName = "ID")},
    inverseJoinColumns = {@JoinColumn(name = "Candidate_Profile_ID",referencedColumnName = "ID")})
    @MapKey(name = "profileName")
    Map<String, CandidateProfile> candidatesProfiles = new HashMap<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "userInfo",orphanRemoval = true)
    Set<EmployerUser> employerUserSet = new HashSet<>();

    public void copyFixedTraitsAndSanitizeFields(UserInfo reference){
        ID=reference.ID;
        email= reference.email;
        OAuth_ID=reference.OAuth_ID;
        candidatesProfiles=reference.candidatesProfiles;
        employerUserSet=reference.employerUserSet;
        geocodingUsage=reference.geocodingUsage;
    }

    //Have better way to do this, possibly make employerUserList a map
    public EmployerUser findEmployerUser(String companyName) throws ResourceNotFoundException {
        companyName=companyName.toLowerCase();
        for (EmployerUser employerUser: employerUserSet) {
            if(employerUser.getEmployer().companyName.toLowerCase().equals(companyName)){
                return employerUser;
            }
        }
        throw new ResourceNotFoundException("Cannot find employerUser with company name "+companyName);
    }

    public boolean hasEmployer(Employer employer){
        return employerUserSet.stream().anyMatch((eu)->eu.getEmployer().equals(employer));
    }

    public EmployerUser findEmployerUser(Employer employer) throws ResourceNotFoundException {
        for (EmployerUser employerUser: employerUserSet) {
            if(employerUser.getEmployer().equals(employer)){
                return employerUser;
            }
        }
        throw new ResourceNotFoundException("Cannot find employerUser with company name "+employer.getCompanyName());
    }

    public void addCandidateProfile(CandidateProfile candidateProfile){
        candidatesProfiles.put(candidateProfile.profileName, candidateProfile);
    }
    @JsonIgnore
    public CandidateProfile  getDefaultCandidateProfile(){
       return candidatesProfiles.get(CandidateProfile.DEFAULT_CANDIDATE_PROFILE);
    }

    public void removeCandidateProfile(String profileName){
        CandidateProfile profile=candidatesProfiles.remove(profileName);
    }

    public boolean hasCandidateProfile(){
        return !candidatesProfiles.isEmpty();
    }

    public void addEmployerUserProfile(EmployerUser employerUser){
        employerUserSet.add(employerUser);
    }

    public boolean isSameUser(UserInfo userInfo){
        return ID.equals(userInfo.ID) && email.equals(userInfo.email) && name.equals(userInfo.name) && OAuth_ID.equals(userInfo.OAuth_ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (age != userInfo.age) return false;
        if (!Objects.equals(ID, userInfo.ID)) return false;
        if (!email.equals(userInfo.email)) return false;
        if (gender != userInfo.gender) return false;
        if (!Objects.equals(phoneNumber, userInfo.phoneNumber))
            return false;
        if (!Objects.equals(name, userInfo.name)) return false;
        if (!OAuth_ID.equals(userInfo.OAuth_ID)) return false;
        if (!Objects.equals(candidatesProfiles, userInfo.candidatesProfiles))
            return false;
        return Objects.equals(employerUserSet, userInfo.employerUserSet);
    }

    @Override
    public int hashCode() {
        int result = ID != null ? ID.hashCode() : 0;
        result = 31 * result + email.hashCode();
        result = 31 * result + (int) age;
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + OAuth_ID.hashCode();
        result = 31 * result + (candidatesProfiles != null ? candidatesProfiles.hashCode() : 0);
        result = 31 * result + (employerUserSet != null ? employerUserSet.hashCode() : 0);
        return result;
    }
}
