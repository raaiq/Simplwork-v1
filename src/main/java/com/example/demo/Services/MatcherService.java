package com.example.demo.Services;

import com.example.demo.Domain.Match;
import com.example.demo.Domain.CandidateProfile;
import com.example.demo.Domain.JobPosting;
import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.*;
import com.example.demo.Domain.TypesAndEnums.Enums.Constants;
import com.example.demo.Domain.TypesAndEnums.Enums.MatchEmployerStatus;
import com.example.demo.Domain.TypesAndEnums.Enums.TransportMode;
import com.example.demo.Exceptions.OperationConditionsFailedException;
import com.example.demo.Exceptions.RouteNotFoundException;
import com.example.demo.Repositories.BranchRepo;
import com.example.demo.Repositories.MatchRepo;
import com.example.demo.Repositories.CandidateRepo;
import com.example.demo.Repositories.JobPostingRepo;
import com.example.demo.Services.Other.GoogleMapsService;
import com.google.maps.errors.UnknownErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
//TODO:Split matching algorithm into separate class
//TODO: Batch distance matrix requests
//TODO:Probably save matches all at once for each new posting/candidate
//TODO: Have mechanism to make sure that all matching is done before application closes
//TODO: Breakup Matcher service into different units
@Service
public class MatcherService implements DisposableBean {


    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private CandidateRepo cRepo;
    @Autowired
    private MatchRepo cpRepo;
    @Autowired
    private JobPostingRepo jobRepo;
    @Autowired
    private GoogleMapsService mapsService;

    private static final String className=MatcherService.class.getName();
    private static final Logger logger= LoggerFactory.getLogger(className);
    private static AtomicBoolean terminate;
    private static BlockingQueue<JobPosting> jobPostingQueue;
    private static BlockingQueue<CandidateProfile> candidateQueue;
    private static final Object signal=new Object();
    private Thread schedulerThread;

    MatcherService(){
        terminate=new AtomicBoolean(false);
        jobPostingQueue=new LinkedBlockingQueue<>();
        candidateQueue= new LinkedBlockingQueue<>();
        schedulerThread= new Thread(this::scheduler);
        logger.info("Starting scheduler thread");
        schedulerThread.start();
    }


     public void matchProfile(CandidateProfile profile){
         candidateQueue.add(profile);
         synchronized (signal){
             logger.debug("Signalling scheduler thread from method 'matchProfile'");
             signal.notifyAll();
         }
    }
     public void matchJobPosting(JobPosting posting){
         jobPostingQueue.add(posting);
         synchronized (signal){
             logger.debug("Signalling scheduler thread from method 'matchPosting'");
             signal.notifyAll();
         }
    }
    //This method assumes only one co-current thread would run this method
    //TODO:Very inefficient. Better method could be to have postings and candidate profiles as nodes in graph and each new node is inserted into graph upon creation
     private void scheduler() {
        int availableThreads=Runtime.getRuntime().availableProcessors();
        int batchSize=Math.max((int)(availableThreads*.7),1);
        int corePoolSize=Math.max((int)(availableThreads*.25),1);

        boolean postingSwitch=false;
        logger.info("Initializing executor service with corePoolSize:{} and maximumPoolSize:{}",corePoolSize,batchSize);
        ExecutorService executorService= new ThreadPoolExecutor(corePoolSize,
                                                                batchSize,
                                                                60,TimeUnit.SECONDS,new LinkedBlockingQueue<>() );

        try {
            while (true) {
                    List<Future<Integer>> submittedTasks;
                    logger.trace("Checking for empty queue in scheduler thread");
                    if (jobPostingQueue.isEmpty() && candidateQueue.isEmpty()) {
                        if (terminate.get()) {
                            break;
                        }
                        logger.debug("Scheduler thread waiting for signal ");
                        synchronized (signal) {
                            signal.wait();
                        }
                        logger.debug("Scheduler thread woken up by signal");
                    }
                    if (postingSwitch) {
                        List<JobPosting> postings = new ArrayList<>();
                        jobPostingQueue.drainTo(postings, batchSize);
                        logger.debug("Matching job posting in scheduler thread");
                        List<Callable<Integer>> callables = postings.stream().map((jp) -> (Callable<Integer>) () -> matchAgainstJob(jp)).collect(Collectors.toList());
                        submittedTasks = executorService.invokeAll(callables);
                    } else {
                        List<CandidateProfile> profiles = new ArrayList<>();
                        candidateQueue.drainTo(profiles, batchSize);
                        logger.debug("Matching candidates in scheduler thread");
                        List<Callable<Integer>> callables = profiles.stream().map((cp) -> (Callable<Integer>) () -> matchAgainstCandidate(cp)).collect(Collectors.toList());
                        submittedTasks = executorService.invokeAll(callables);
                    }
                    postingSwitch = !postingSwitch;

                    logger.info("Futures list size is {}", submittedTasks.size());

                    for (Future<Integer> f : submittedTasks) {
                        try{

                            logger.info("Executing future: {}",f);
                            f.get();
                        } catch (ExecutionException e) {
                            logger.error("Exception occurred in executing a future "+ f,e);
                        }
                    }

            }
        }catch (InterruptedException e){
            logger.error("Exception occurred in 'scheduler()' with exception", e);
        }
         logger.info("Initiating shutdown for scheduler thread ");
         executorService.shutdown();
         try {
             if(!executorService.awaitTermination(5 ,TimeUnit.SECONDS)){
                 logger.error("Unable to terminate executor service");
             }
         } catch (InterruptedException e) {
             logger.info("InterruptedException thrown while waiting for termination of executor service", e);
         }
         logger.info("Executor service shutdown");
         
    }

    //TODO: Potential co-currency problem where the candidate posting is already created by opposing matching method, because the entity is already in database
    //Does actual job matching
    private int matchAgainstCandidate(CandidateProfile cand){
        final String methodName="matchAgainstCandidate";
        if(!cand.getAutoMatch()){ return 0;}
        Location.LocationBounds boundary=cand.getLocationBoundaries();

        List<JobPosting> postings= jobRepo.findByBranch_Location_LatitudeBetweenAndBranch_Location_LongitudeBetween(boundary.latitudeLow,
                                                                                                                    boundary.latitudeHigh,
                                                                                                                    boundary.longitudeLow,
                                                                                                                    boundary.longitudeHigh);

        for (JobPosting posting:postings) {
            if(cpRepo.existsByKey(new Match.CompositeKey(cand.getID(),posting.getID()))){
                continue;
            }
            Optional<Match> entry;
            try {
                entry=match(cand,posting,false);
            }catch (OperationConditionsFailedException e){
                continue;
            }
            Match match =entry.get();
            match.setEmployerStatus(MatchEmployerStatus.NEW);
            cpRepo.save(match);
        }
        return 1;
    }

    // Does actual job matching
    private int matchAgainstJob(JobPosting posting){
        final String methodName="matchAgainstJob";

        double maxSearchDistance= TransportMode.CAR.maxTravelDistance;
        maxSearchDistance *= Constants.DISTANCE_RADIUS_MULTIPLIER.getValue();
        Location postingLocation= posting.getBranch().getLocation();
        int latRad= (int) Location.kmToLatitude(maxSearchDistance,postingLocation.getLatitude()),
                longRad= (int)Location.kmToLongitude(maxSearchDistance,postingLocation.getLatitude());

        List<CandidateProfile> candidateProfiles = cRepo.findByLocation_LatitudeBetweenAndLocation_LongitudeBetweenAndAutoMatch(postingLocation.latitude - latRad ,postingLocation.latitude + latRad,
                                                                                                                            postingLocation.longitude - longRad, postingLocation.longitude + longRad, true);

        for (CandidateProfile candidateProfile : candidateProfiles) {
            if(cpRepo.existsByKey(new Match.CompositeKey(candidateProfile.getID(),posting.getID()))){
                continue;
            }
            Optional<Match> entry;
            try{entry=match(candidateProfile,posting,false);}
            catch (OperationConditionsFailedException e){
                continue;
            }
            var candidatePosting= entry.get();
            candidatePosting.setEmployerStatus(MatchEmployerStatus.NEW);
            cpRepo.save(candidatePosting);
        }
        return 1;
    }

    //TODO:Account for transportation methods
    //TODO:Have options with the highest similarity for candidates if matches are low
    //TODO:Stop matching after certain number of matches per candidate
    public Optional<Match> match(CandidateProfile candidate, JobPosting posting, boolean manualMatching) throws OperationConditionsFailedException {

        Match match = new Match(candidate,posting);

        match.setManualMatch(manualMatching);

        Location candLoc= candidate.getLocation();
        Location jobLocation= posting.getBranch().getLocation();
        double distance=Location.getDistance(candLoc, jobLocation);

        //TODO:Make calculation happen only once per candidate
        var candTravelTimes=candidate.getMaxTravelTimes();
        TransportMode fastestMode;
        try {
            fastestMode= getFastestMode(candTravelTimes);
        }catch (Exception e){
            logger.error("Invalid code path reached, no transport mode available for candidate");
            return Optional.empty();
        }
        double maxTravelDistance= fastestMode.getDistanceCovered(candTravelTimes.get(fastestMode));

        if(! manualMatching && distance > maxTravelDistance){
            throw new OperationConditionsFailedException("Cannot match: job location too far");
        }

        match.setDistanceToWork(distance);
        List<ShiftCompatibility> shiftCompatibilityList= matchShifts(candidate.getAvailability(), new ArrayList<>(posting.getShifts()),posting.getFixedSchedule());

        Pair<Double,Double> hours= getTotalAndCompatibleHours(shiftCompatibilityList,posting.getShifts());

        match.setShiftCompatibilityList(shiftCompatibilityList);
        if(shiftCompatibilityList.isEmpty()){
            throw new OperationConditionsFailedException("Cannot match: no matching shifts");
        }

        int matchingHours= (int)Math.round(hours.getSecond());
        int jobHours= posting.getFixedSchedule() ? matchingHours : posting.getEstimatedHours();

        if(!manualMatching && jobHours > candidate.getMaximumHours()*Constants.MAXIMUM_HOURS_BUFFER.getValue()){
            throw new OperationConditionsFailedException("Cannot match: job's required shifts exceeds candidate's available hours");
        }


        getCommuteTimes(match,manualMatching);

        if(match.getShiftCompatibilityList().isEmpty()){
            throw new OperationConditionsFailedException("Cannot match: job location too far, try increasing commute time or add more modes ");
        }


        match.setShiftScore((int)(hours.getSecond()/hours.getFirst()*100));

        match.setMatchingHours(matchingHours);
        match.setPotentialEarnings(posting.getFixedSchedule()  || posting.getEstimatedHours() >  hours.getSecond().intValue() ? match.getMatchingHours() : posting.getEstimatedHours());
        match.setPotentialEarnings((int)(match.getPotentialEarnings()*posting.getPay()));
        match.setCandScore(match.getPotentialEarnings());
        double empScore= match.getShiftScore();
        empScore= empScore > 70 && match.getDistanceToWork() <= 10 ? empScore*2 : empScore;
        match.setEmpScore((int)empScore);
        return Optional.of(match);
    }
    //TODO: Maybe replace Map with linked list ranked from fastest to slowest
    TransportMode getFastestMode(Map<TransportMode,Integer> modes) throws Exception {
        TransportMode[] availableModes=TransportMode.values() ;
        for (int i=0; i<availableModes.length;i++){
            if(modes.containsKey(availableModes[i])){
                return availableModes[i];
            }
        }
        throw new Exception("No transport mode available in enum map");
    }

    public static List<ShiftCompatibility> matchShifts(Timeslots<ArrayList<TimePair>> candTimes, List<Shift> jobShifts,boolean fixedSchedule){

        Collections.sort(jobShifts);
        ListIterator<Shift> jTimes=jobShifts.listIterator();
        ArrayList<ShiftCompatibility> shiftCompatibilities=new ArrayList<>();

        for (int i=1;i<=7;i++){
            DayOfWeek dayOfWeek= DayOfWeek.of(i);
            ArrayList<TimePair> timePairArray=candTimes.get(dayOfWeek);

            if(timePairArray == null || timePairArray.isEmpty()){
                continue;
            }
            if(calculateShiftCompatibility(shiftCompatibilities,timePairArray.listIterator(),jTimes,i,fixedSchedule)){
                break;
            }

        }

        return  shiftCompatibilities;


    }
    //TODO: Algorithm can be simpler, maybe
    //TODO: Account for if shift ends past midnight
    public static boolean calculateShiftCompatibility(ArrayList<ShiftCompatibility> compatibilityList, ListIterator<TimePair> cTimeItr,ListIterator<Shift> jTimeItr,int dayOfWeek,boolean fixedSchedule){
        if(!cTimeItr.hasNext() || !jTimeItr.hasNext()){
            if(!cTimeItr.hasNext() && jTimeItr.hasNext() && fixedSchedule){
                compatibilityList.clear();
            }
            return true;
        }
        Shift jShift=jTimeItr.next();
        if(jShift.getDayOfWeek() != dayOfWeek){
            jTimeItr.previous();
            return false;
        }

        TimePair jTime=jShift.getShiftTimes();
        TimePair cTime=cTimeItr.next();
        jTimeItr.previous();
        cTimeItr.previous();

        if(cTime.endTime < jTime.startTime){
            cTimeItr.next();
            return calculateShiftCompatibility(compatibilityList,cTimeItr,jTimeItr,dayOfWeek,fixedSchedule);
        }
        if(cTime.startTime > jTime.endTime){
            if(fixedSchedule){
                compatibilityList.clear();
                return true;
            }
            jTimeItr.next();
            return calculateShiftCompatibility(compatibilityList,cTimeItr,jTimeItr,dayOfWeek,fixedSchedule);
        }

        int minAfterShift= Math.max(0,cTime.startTime-jTime.startTime);
        int minTillEnd=jTime.endTime - Math.min(jTime.endTime,cTime.endTime);

        //TODO: Have percentage threshold
        if(minTillEnd+minAfterShift >= 2*60){
            if(fixedSchedule){
                compatibilityList.clear();
                return true;
            }
            jTimeItr.next();
            return calculateShiftCompatibility(compatibilityList,cTimeItr,jTimeItr,dayOfWeek, false);
        }

        ShiftCompatibility compatibility=new ShiftCompatibility();
        compatibility.setShift(jShift);
        compatibility.minAfterShift =minAfterShift;
        compatibility.minTillEnd =minTillEnd;
        compatibilityList.add(compatibility);
        jTimeItr.next();
        return calculateShiftCompatibility(compatibilityList,cTimeItr,jTimeItr,dayOfWeek,fixedSchedule);
    }

    public static Pair<Double,Double> getTotalAndCompatibleHours(List<ShiftCompatibility> compatibilityList, List<Shift> shifts){
        double matchingMin, jobMin;

        jobMin = shifts.stream().reduce(0,
                                (i,e)-> i + (e.getShiftTimes().endTime - e.getShiftTimes().startTime),
                                Integer::sum);

        matchingMin= compatibilityList.stream().reduce(0,
                (i,e)-> i + (e.getShift().getShiftTimes().endTime - e.getShift().getShiftTimes().startTime) - e.minAfterShift - e.minTillEnd,
                Integer::sum);

        return Pair.of(jobMin/60,matchingMin/60);
    }



    public static boolean canCommute(TransportMode mode,Map<TransportMode,Integer> maxTravelTimes,double distance){
        return maxTravelTimes.containsKey(mode) && mode.getDistanceCovered(maxTravelTimes.get(mode)) >= distance;
    }
    public void getCommuteTimes(Match match, boolean manualMatch){

        Location jobLocation= match.getPosting().getBranch().getLocation();
        Location candidateLoc= match.getCandidateProfile().getLocation();
        Map<TransportMode,Integer> candidateMaxTravelTimes= match.getCandidateProfile().getMaxTravelTimes();

        //TODO: Redundant to recalculate distance if already done
        double distance=  match.getDistanceToWork();
        Shift sampleShift=match.getShiftCompatibilityList().get(0).getShift();
        ZonedDateTime localTime= ZonedDateTime.now(ZoneId.of("America/Toronto"));

        localTime= localTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(sampleShift.getDayOfWeek())));

        //TODO:Check conversion is valid
        TimePair shiftTimes= sampleShift.getShiftTimes();
        Instant arrivalTime= Instant.from(localTime.withHour(shiftTimes.startTime/60).withMinute(shiftTimes.startTime%60).withSecond(0));

        int commuteTime;

        if(canCommute(TransportMode.CAR,candidateMaxTravelTimes,distance)){
            try {
                commuteTime = mapsService.calculateCommuteTime(candidateLoc,jobLocation,TransportMode.CAR,Optional.of(arrivalTime),null);
                match.setCarCommuteTime(commuteTime <= candidateMaxTravelTimes.get(TransportMode.CAR) || manualMatch ? commuteTime : null);
            }catch (RouteNotFoundException | UnknownErrorException e){}
        }
        if(canCommute(TransportMode.BIKE,candidateMaxTravelTimes,distance)){
            try {
                commuteTime = mapsService.calculateCommuteTime(candidateLoc,jobLocation,TransportMode.BIKE,Optional.of(arrivalTime),null);
                match.setBikeCommuteTime(commuteTime <= candidateMaxTravelTimes.get(TransportMode.BIKE) || manualMatch ? commuteTime : null);
            }catch (RouteNotFoundException | UnknownErrorException e){}
        }
        if(canCommute(TransportMode.WALK,candidateMaxTravelTimes,distance)){
            try {

                commuteTime = mapsService.calculateCommuteTime(candidateLoc,jobLocation,TransportMode.WALK,Optional.of(arrivalTime),null);

                match.setWalkCommuteTime(commuteTime <= candidateMaxTravelTimes.get(TransportMode.WALK) || manualMatch ? commuteTime : null);
            }catch (RouteNotFoundException | UnknownErrorException e){}
        }
        if(match.getCarCommuteTime() !=null || match.getBikeCommuteTime() !=null || match.getWalkCommuteTime() != null){
            return;
        }
        if(!candidateMaxTravelTimes.containsKey(TransportMode.PUBLIC_TRANSIT)){
            match.getShiftCompatibilityList().clear();
            return;
        }

        mapsService.getPublicTransitTimes(match.getShiftCompatibilityList(),candidateLoc,jobLocation,candidateMaxTravelTimes.get(TransportMode.PUBLIC_TRANSIT),manualMatch);

    }

    @Override
    public void destroy() throws InterruptedException {
        logger.trace("Signalling shutdown of scheduler thread");
        terminate.set(true);
        synchronized (signal){
            signal.notifyAll();
        }
        logger.info("Waiting termination of scheduler thread");
        schedulerThread.join(7000);
        logger.info("Scheduler thread terminated");
    }

}

