package com.github.solenra.server.service;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.model.EnergyPlanDto;

public interface EnergyPlanService {

    void calculateAndSaveEnergyRevenue(SystemEnergyDetails systemEnergyDetails, long energyDetailsHourDuration);

    void updateEnergyPlanRevenueCalculationNewTransaction(long solarSystemIntegration);

    void recalculateSolarSystemRevenue(long solarSystemId);

    EnergyPlanDto getEnergyPlan(Long id);

    EnergyPlanDto saveEnergyPlan(EnergyPlanDto energyPlan);

    void deleteEnergyPlan(Long id);

    Page<EnergyPlanDto> searchEnergyPlans(Principal principal, Long energyPlanId, Pageable pageable);

}
