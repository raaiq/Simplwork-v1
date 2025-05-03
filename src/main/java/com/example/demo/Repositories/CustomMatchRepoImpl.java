package com.example.demo.Repositories;

import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.Match;
import com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

@Component
public class CustomMatchRepoImpl implements CustomMatchRepo {

    @PersistenceContext
    private EntityManager manager;

    //TODO:Optimize query
    //TODO:Provide escape character for is like clause, check trace logs
    //Since query is in bound by location, user may not be able search for jobs outside of box

    public List<MatchPostingPair> searchForPostings(double latitudeLow, double latitudeHigh, double longitudeLow, double longitudeHigh,
                                                    long candidateID, String queryString, Pageable pageable){

        String JPAQuery="select NEW com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair(jp,match) " +
                "from JobPosting jp JOIN jp.branch br JOIN br.company emp " +
                "LEFT JOIN Match match ON jp.ID = match.key.postingId " +
                "WHERE match.key.candidateId = :candidateID OR ( match.key.candidateId IS NULL AND " +
                "br.location.latitude between :latitudeLow and :latitudeHigh  AND br.location.longitude between :longitudeLow and :longitudeHigh AND " +
                "(lower(emp.companyName) LIKE lower(:queryString) OR lower(jp.positionTitle) LIKE lower(:queryString))) ORDER BY ";

        Sort.Order order= pageable.getSort().stream().iterator().next();
        //TODO: Make sure order property string is sanitary
        JPAQuery += order.getProperty();
        JPAQuery += order.isAscending() ? " ASC ": " DESC ";
        JPAQuery += "NULLS LAST";
        TypedQuery<MatchPostingPair> query= manager.createQuery(JPAQuery,MatchPostingPair.class);
        query.setParameter("latitudeLow",latitudeLow)
                .setParameter("latitudeHigh",latitudeHigh)
                .setParameter("longitudeLow",longitudeLow)
                .setParameter("longitudeHigh",longitudeHigh)
                .setParameter("candidateID",candidateID)
                .setParameter("queryString",queryString);
        query.setFirstResult(pageable.getPageSize()* pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }
//
//    public long deleteByCandidateProfile(CandidateProfile profile){
//
//    }
}
