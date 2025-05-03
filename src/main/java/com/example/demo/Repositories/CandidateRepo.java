package com.example.demo.Repositories;

import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.TypesAndEnums.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepo extends JpaRepository<CandidateProfile,Long> {

    List<CandidateProfile> findCandidateByLocationBetweenAndAutoMatchTrue(Location upperBound, Location lowerBound);

    List<CandidateProfile> findByLocation_LatitudeBetweenAndLocation_LongitudeBetweenAndAutoMatch(double latitudeStart, double latitudeEnd, double longitudeStart, double longitudeEnd, boolean autoMatch);
}
