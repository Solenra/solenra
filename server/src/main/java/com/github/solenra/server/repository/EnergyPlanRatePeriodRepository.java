package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.solenra.server.entity.EnergyPlanRatePeriod;
import com.github.solenra.server.entity.SolarSystem;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public interface EnergyPlanRatePeriodRepository extends JpaRepository<EnergyPlanRatePeriod, Long> {

    @Query("select eprp" +
            " from EnergyPlanRatePeriod eprp" +
            "   join eprp.daysOfWeek dow" +
            "   join eprp.energyPlanRate epr" +
            "   join epr.energyPlan ep" +
            "   join ep.solarSystemEnergyPlans ssep" +
            " where ssep.solarSystem = :solarSystem" +
            " and :startDate >= ssep.startDate" +
            " and (" +
            "     :endDate <= ssep.endDate" +
            "     or ssep.endDate is null" +
            " )" +
            " and dow.dayOfWeek = :dayOfWeek" +
            " and :startTime >= eprp.startTime" +
            " and :endTime <= eprp.endTime")
    EnergyPlanRatePeriod findByEnergyPlanAndEffectiveDates(
            @Param("solarSystem") SolarSystem solarSystem,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

}
