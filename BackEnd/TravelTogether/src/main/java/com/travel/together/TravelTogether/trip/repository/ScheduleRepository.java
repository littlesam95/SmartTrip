package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
//    List<Schedule> findByTripIdOrderByDayAscOrderAsc(Long tripId);
}