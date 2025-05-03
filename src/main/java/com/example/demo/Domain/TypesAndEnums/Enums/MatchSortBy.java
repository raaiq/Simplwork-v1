package com.example.demo.Domain.TypesAndEnums.Enums;

//TODO: Remove redundant enum and use property names directly instead
public enum MatchSortBy {
    PAY("jp.pay","pay"),
    COMPANY_NAME("emp.companyName","companyName"),
    POSITION_TITLE("jp.positionTitle","positionTitle"),
    DISTANCE("match.distanceToWork","distanceToWork"),
    MATCHING_HOURS("match.matchingHours","matchingHours"),
    POTENTIAL_EARNINGS("match.potentialEarnings","potentialEarnings"),
    CANDIDATE_SCORE("match.candScore","candScore"),
    EMPLOYER_SCORE("match.empScore","empScore"),
    SHIFT_SCORE("match.shiftScore","shiftScore");

    public final String JPAAnalogCompositeAnalog;
    public final String JPASimpleAnalog;

    MatchSortBy(String jpaAnalog, String jpaSimpleAnalog) {
        JPAAnalogCompositeAnalog = jpaAnalog;
        JPASimpleAnalog= jpaSimpleAnalog;
    }
}
