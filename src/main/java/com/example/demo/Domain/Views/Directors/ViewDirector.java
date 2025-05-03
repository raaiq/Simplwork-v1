package com.example.demo.Domain.Views.Directors;

import com.example.demo.Domain.*;
import com.example.demo.Domain.TypesAndEnums.Compositions.MatchPostingPair;
import com.example.demo.Domain.TypesAndEnums.Compositions.CandidateUser;
import com.example.demo.Domain.TypesAndEnums.JacksonMixins.*;
import com.example.demo.Domain.TypesAndEnums.Location;
import com.example.demo.Domain.Views.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.maps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class ViewDirector {

    static ObjectMapper mapper;
    static ObjectWriter writer;
    static Logger logger = LoggerFactory.getLogger(ViewDirector.class);

    static {
        mapper= new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(DirectionsLeg.class, DirectionLegMixin.class)
                .addMixIn(DirectionsStep.class, DirectionStepMixin.class)
                .addMixIn(TransitDetails.class,TransitDetailsMixin.class)
                .addMixIn(Duration.class, DurationMixin.class)
                .addMixIn(Distance.class, DistanceMixin.class)
                .addMixIn(TransitLine.class, TransitLineMixin.class)
                .addMixIn(TransitAgency.class, TransitAgencyMixin.class);
        writer= mapper.writer();
    }
    public static JobPostingView getJobPostingBasicView(JobPosting posting){
        JobPostingView view= new JobPostingView(posting,true);

        List<ShiftView> shifts=posting.getShifts().stream().map(ShiftView::new).collect(Collectors.toList());
        view.setShifts(shifts);
        return view;
    }

    //TODO: Add job location as a String
    public static JobPostingView getJobPostingCandidateView(JobPosting posting){
        JobPostingView baseView=getJobPostingBasicView(posting);

        EmployerView employerView= new EmployerView();
        Employer employer= posting.getBranch().getCompany();
        employerView.setCompanyName(employer.getCompanyName());
        employerView.setCompanyDescription(employer.getCompanyDescription());
        EmployerImageFile imageFile= employer.getCompanyImage();
        if(imageFile !=null){
        employerView.setImageID(imageFile.getImageID());
        }
        baseView.setEmployer(employerView);

        BranchView branchView= new BranchView();
        branchView.branchName=posting.getBranch().getBranchName();
        branchView.location= posting.getBranch().getLocation();
        employerView.setBranches(List.of(branchView));
        return baseView;
    }

    //TODO: Add branch location as String
    public static JobPostingView getJobPostingEmployerView(JobPosting posting){
        JobPostingView view= getJobPostingBasicView(posting);
        BranchView branchView= new BranchView();
        Branch branch= posting.getBranch();
        branchView.setBranchName(branch.getBranchName());
        branchView.location=branch.getLocation();
        view.setBranch(branchView);
        return view;
    }

    public static List<MatchView> getMatchCandidateView(List<MatchPostingPair> list,CandidateProfile candidateProfile){
        return list.stream().map(i->getMatchCandidateView(i,candidateProfile)).collect(Collectors.toList());
    }

    public static List<MatchView> getMatchEmployerView(List<Pair<Match,UserInfo>> list){
        return list.stream().map(i-> getMatchEmployerView(i.getFirst(),i.getSecond())).toList();
    }

    public static MatchView getMatchCandidateView(MatchPostingPair pair,CandidateProfile candidateProfile){
        MatchView view;
        if(pair.match == null){
            view= new MatchView();
            view.setPosting(getJobPostingCandidateView(pair.getJobPosting()));
            view.setDistance(Location.getDistance(candidateProfile.getLocation(),pair.getJobPosting().getBranch().getLocation()));
        }else {
            view= getMatchCandidateView(pair.getMatch());
        }
        return view;
    }

    //TODO:Do shift calculations on backend
    public static MatchView getMatchCandidateView(Match match){
        MatchView view= new MatchView(match,false);
        view.setPosting(getJobPostingCandidateView(match.getPosting()));
        List<ShiftCompatibilityView> shiftViews= match.getShiftCompatibilityList().stream().map(i->new ShiftCompatibilityView(i,false)).toList();
        view.setShiftCompatibilities(shiftViews);
        return view;
    }
    //TODO:Better way to calculate distance
    //TODO:Don't disclose travel cost and mode of transit to employer
    public static MatchView getMatchEmployerView(Match match,UserInfo candidateUser){
        MatchView view= new MatchView(match,true);
        CandidateProfileView candidateView = getCandidateEmployerView(new CandidateUser(match.getCandidateProfile(),candidateUser));
        view.setCandidateProfile(candidateView);

        JobPostingView postingView=new JobPostingView();
        postingView.setID(match.getPosting().getID());
        postingView.setBranch(new BranchView(match.getPosting().getBranch()));
        view.setPosting(postingView);

        List<ShiftCompatibilityView> shiftViews= match.getShiftCompatibilityList().stream().map(i->new ShiftCompatibilityView(i,false)).toList();
        view.setShiftCompatibilities(shiftViews);
        return view;

    }


    public static EmployerView getEmployerDetailedView(Employer employer, List<Pair<Branch,List<JobPosting>>> branchAndPostings) throws IllegalArgumentException{

        EmployerView view= new EmployerView();
        view.setCompanyDescription(employer.getCompanyDescription());
        view.setCompanyName(employer.getCompanyName());
        var imageFile= employer.getCompanyImage();
        if(imageFile !=null){
        view.setImageID(imageFile.getImageID());
        }

        if(!branchAndPostings.isEmpty()){
        view.setBranches(branchAndPostings.stream().map( i -> {
            Branch branch= i.getFirst();
            if(branch.getCompany() !=employer){
                IllegalArgumentException exception= new IllegalArgumentException ("Branch " + branch.getBranchName() +"with ID "+ branch.getID()+ "has no employer " + employer.getCompanyName() + "with ID " + employer.getID());
                logger.warn("Error in method 'getEmployerProfileDetailedView' ",exception );
                throw  exception;
            }
            return ViewDirector.getBranchView(i.getFirst(),i.getSecond());
        }).collect(Collectors.toList()));
        }
        return view;
    }

    public static  EmployerUserView getEmployerUserView(EmployerUser employerUser){
        return new EmployerUserView(employerUser);
    }

    public static  BranchView getBranchView(Branch branch, List<JobPosting> postingList) throws IllegalArgumentException{
        BranchView view= new BranchView();
        view.setBranchName(branch.getBranchName());
        view.location=branch.getLocation();
        if(!postingList.isEmpty())
        {
        List<JobPostingView> postingViews= postingList.stream().
                map(p->{
                    if(branch !=p.getBranch()){
                        IllegalArgumentException exception= new IllegalArgumentException ("JobPosting with ID: " + p.getID()+ "has no branch: " + branch.getBranchName() + "with ID " + branch.getID());
                        logger.warn("Error in method 'getBranchView' ",exception );
                        throw  exception;
                    }
                    return new JobPostingView(p,true);})
                .collect(Collectors.toList());

        view.setJobs(postingViews);
        }
        return view;
    }

    public static CandidateProfileView getCandidateProfilePrivateView(CandidateUser candidate){
        CandidateProfile profile=candidate.getCandidateProfile();
        UserInfo user=candidate.getUser();
        CandidateProfileView view= new CandidateProfileView();
        view.setAutoMatch(profile.getAutoMatch());
        view.setMaxTravelTimes(profile.getMaxTravelTimes());
        view.setAvailability(profile.getAvailability());
        view.setWorkHistory(profile.getWorkHistory());
        view.setFullAddress(profile.getLocation().fullAddress);
        view.setAge(user.getAge());
        view.setEmail(user.getEmail());
        view.setMaximumHours(profile.getMaximumHours());
        view.setGender(user.getGender());
        view.setPhoneNumber(user.getPhoneNumber());
        view.setCandidateName(user.getName());
        return view;
    }

    //TODO: Maybe send vague location of candidate
    public static CandidateProfileView getCandidateEmployerView(CandidateUser candidate){
        CandidateProfile profile= candidate.getCandidateProfile();
        UserInfo user= candidate.getUser();
        CandidateProfileView view= new CandidateProfileView();
        view.setAvailability(profile.getAvailability());
        view.setWorkHistory(profile.getWorkHistory());
        view.setAge(user.getAge());
        view.setEmail(user.getEmail());
        view.setGender(user.getGender());
        view.setPhoneNumber(user.getPhoneNumber());
        view.setCandidateName(user.getName());
        view.setID(profile.getID());

        return view;

    }

    //TODO: Replace with spring managed object mapper if possible
    public static String convertToString(Object o) throws JsonProcessingException {
            return writer.writeValueAsString(o);
    }

    public static <T> T converttoObject(String json,Class<T> type) throws JsonProcessingException {
        return mapper.readValue(json,type);
    }

    public static <T> T converttoObject(String json, TypeReference<T> type) throws JsonProcessingException {


        return mapper.readValue(json,type);
    }

}
