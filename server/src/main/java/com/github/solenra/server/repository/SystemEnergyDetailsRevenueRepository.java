package com.github.solenra.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.solenra.server.entity.EnergyPlan;
import com.github.solenra.server.entity.SolarSystem;
import com.github.solenra.server.entity.SystemEnergyDetailsRevenue;
import com.github.solenra.server.model.SolarSystemEnergyPlanDto;

import java.util.List;

public interface SystemEnergyDetailsRevenueRepository extends JpaRepository<SystemEnergyDetailsRevenue, Long> {

    List<SystemEnergyDetailsRevenue> findAllBySystemEnergyDetailsSolarSystemIntegrationId(long solarSystemIntegrationId);

    Page<SystemEnergyDetailsRevenue> findAllBySystemEnergyDetailsSolarSystemIntegrationSolarSystemId(Long solarSystemId, Pageable pageable);

    @Query("select new com.github.solenra.server.model.SolarSystemEnergyPlanDto(" +
            "   sum(sedr1.supplyCost)," +
            "   sum(sedr1.importCost)," +
            "   sum(sedr1.exportRevenue)," +
            "   sum(sedr1.selfConsumptionSavings)" +
            " )" +
            " from SystemEnergyDetailsRevenue sedr1" +
            "   join sedr1.energyPlanRatePeriod eprp1" +
            "       join eprp1.energyPlanRate epr1" +
            "   join sedr1.systemEnergyDetails sed1" +
            "       join sed1.solarSystemIntegration ssi1" +
            " where epr1.energyPlan = :energyPlan" +
            " and ssi1.solarSystem = :solarSystem")
    SolarSystemEnergyPlanDto sumFieldsBySolarSystemEnergyPlan(
            @Param("solarSystem") SolarSystem solarSystem,
            @Param("energyPlan") EnergyPlan energyPlan
    );

    int deleteAllBySystemEnergyDetailsSolarSystemIntegrationId(Long solarSystemIntegrationId);

}
