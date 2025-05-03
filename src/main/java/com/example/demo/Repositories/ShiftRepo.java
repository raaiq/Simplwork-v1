package com.example.demo.Repositories;

import com.example.demo.Domain.Branch;
import com.example.demo.Domain.Shift;
import com.example.demo.Domain.TypesAndEnums.TimePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO:Check if methods are actually dependent on branch id
@Repository
public interface ShiftRepo extends JpaRepository<Shift,UUID>{
    Optional<Shift> findByDayOfWeekAndShiftTimes_StartTimeAndShiftTimes_EndTimeAndBranch_ID(Integer dayOfWeek, Short startTime, Short endTime, long ID);

    boolean existsByShiftTimesAndDayOfWeek(TimePair shiftTimes, int dayOfWeek);

    boolean existsByShiftTimes_StartTimeAndShiftTimes_EndTimeAndDayOfWeekAndBranch_ID(int startTime, int endTime, int dayOfWeek, long branchID);

    @Transactional
    long deleteByBranch(Branch branch);



}
