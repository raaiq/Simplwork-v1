package com.example.demo.Repositories;

import com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomMatchRepo {

    List<MatchPostingPair> searchForPostings(double latitudeLow, double latitudeHigh, double longitudeLow, double longitudeHigh,
                                             long candidateID, String queryString, Pageable pageable);
}
