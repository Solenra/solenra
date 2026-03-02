package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.solenra.server.entity.EnergyPlan;
import com.github.solenra.server.entity.SolarSystem;
import com.github.solenra.server.entity.SolarSystemEnergyPlan;
import com.github.solenra.server.model.SolarSystemDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SolarSystemEnergyPlanRepository extends JpaRepository<SolarSystemEnergyPlan, Long> {

    @Query("select ssep" +
            " from SolarSystemEnergyPlan ssep" +
            " where ssep.solarSystem = :solarSystem" +
            " and :startDate >= ssep.startDate" +
            " and (" +
            "     :endDate <= ssep.endDate" +
            "     or :endDate is null" +
            " )")
    List<SolarSystemEnergyPlan> findAllBySolarSystemAndEffectiveDates(
            @Param("solarSystem") SolarSystem solarSystem,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    SolarSystemEnergyPlan findBySolarSystemAndEnergyPlanAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            SolarSystem solarSystem,
            EnergyPlan energyPlan,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<SolarSystemEnergyPlan> findAllBySolarSystemAndIncludeInRevenueCalculation(SolarSystem solarSystem, boolean includeInRevenueCalculation);

    List<SolarSystemEnergyPlan> findAllBySolarSystemId(long solarSystemIntegrationId);

    @Query("select new com.github.solenra.server.model.SolarSystemDto(" +
            "   sum(ssep1.cumulativeSupplyCost)," +
            "   sum(ssep1.cumulativeImportCost)," +
            "   sum(ssep1.cumulativeExportRevenue)," +
            "   sum(ssep1.cumulativeSelfConsumptionSavings)" +
            " )" +
            " from SolarSystemEnergyPlan ssep1" +
            " where ssep1.solarSystem = :solarSystem")
    SolarSystemDto sumFieldsBySolarSystem(
            @Param("solarSystem") SolarSystem solarSystem
    );

}
