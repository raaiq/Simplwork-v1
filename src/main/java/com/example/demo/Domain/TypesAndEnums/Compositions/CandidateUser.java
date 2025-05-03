package com.example.demo.Domain.TypesAndEnums.Compositions;

import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.UserInfo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateUser {
    //Check if valid constraint is needed
    @Valid
    CandidateProfile candidateProfile;
    @Valid
    UserInfo user;



    public void copyFixedTraitsAndSanitizeFields(CandidateProfile referenceProfile,UserInfo referenceUser){
        user.copyFixedTraitsAndSanitizeFields(referenceUser);
        candidateProfile.copyFixedTraitsAndSanitizeFields(referenceProfile);
    }

}
