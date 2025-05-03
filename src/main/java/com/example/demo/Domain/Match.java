package com.example.demo.Domain;


import com.example.demo.Domain.TypesAndEnums.Enums.MatchCandidateStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Domain.TypesAndEnums.ShiftCompatibility;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Match {

    @EmbeddedId
    CompositeKey key;

    @ManyToOne
    @MapsId("postingId")
    @JoinColumn(name="postingId")
    JobPosting posting;

    @ManyToOne
    @MapsId("candidateId")
    @JoinColumn(name = "candidateId")
    CandidateProfile candidateProfile;

    Integer walkCommuteTime=null,
            bikeCommuteTime=null,
            carCommuteTime=null;

    //in Km
    double distanceToWork;

    boolean manualMatch=false;

    int shiftScore;

    private MatchEmployerStatus employerStatus = MatchEmployerStatus.NEW;
    private MatchCandidateStatus candidateStatus = MatchCandidateStatus.APPLIED;

    int matchingHours,potentialEarnings,candScore,empScore;

    @ElementCollection(fetch = FetchType.EAGER)
    List<ShiftCompatibility> shiftCompatibilityList= new ArrayList<>();

    public Match(CandidateProfile candidateProfile, JobPosting posting){
        key= new CompositeKey(candidateProfile.ID,posting.ID);
        this.posting=posting;
        this.candidateProfile = candidateProfile;
    }

    public void setScores(int[] scores){
        shiftScore= scores[2];
        potentialEarnings = scores[3];
        empScore=scores[4];

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (walkCommuteTime != match.walkCommuteTime) return false;
        if (bikeCommuteTime != match.bikeCommuteTime) return false;
        if (carCommuteTime != match.carCommuteTime) return false;
        if (Double.compare(match.distanceToWork, distanceToWork) != 0) return false;
        if (shiftScore != match.shiftScore) return false;
        if (potentialEarnings != match.potentialEarnings) return false;
        if (candScore != match.candScore) return false;
        if (empScore != match.empScore) return false;
        if (!Objects.equals(key, match.key)) return false;
        if (!Objects.equals(posting, match.posting)) return false;
        if (!Objects.equals(candidateProfile, match.candidateProfile))
            return false;
        if (employerStatus != match.employerStatus) return false;
        if( manualMatch != match.manualMatch) return false;
        if (candidateStatus != match.candidateStatus) return false;
        return Objects.equals(shiftCompatibilityList, match.shiftCompatibilityList);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = key != null ? key.hashCode() : 0;
        result = 31 * result + (posting != null ? posting.hashCode() : 0);
        result = 31 * result + (candidateProfile != null ? candidateProfile.hashCode() : 0);
        result = 31 * result + walkCommuteTime;
        result = 31 * result + bikeCommuteTime;
        result = 31 * result + carCommuteTime;
        temp = Double.doubleToLongBits(distanceToWork);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + shiftScore;
        result = 31 * result + (employerStatus != null ? employerStatus.hashCode() : 0);
        result = 31 * result + (candidateStatus != null ? candidateStatus.hashCode() : 0);
        result = 31 * result + potentialEarnings;
        result = 31 * result + candScore;
        result = 31 * result + empScore;
        result = 31 * result + (manualMatch ? 1: 0);
        result = 31 * result + (shiftCompatibilityList != null ? shiftCompatibilityList.hashCode() : 0);
        return result;
    }


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompositeKey implements Serializable {

        long candidateId;

        long postingId;
    }


}

