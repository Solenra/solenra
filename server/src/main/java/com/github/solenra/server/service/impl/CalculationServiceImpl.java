package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.repository.integration.SystemEnergyDetailsRepository;
import com.github.solenra.server.service.CalculationService;
import com.github.solenra.server.service.EnergyPlanService;

@Service("calculationService")
public class CalculationServiceImpl implements CalculationService {

    private final SystemEnergyDetailsRepository systemEnergyDetailsRepository;
    private final EnergyPlanService energyPlanService;

    public CalculationServiceImpl(
            SystemEnergyDetailsRepository systemEnergyDetailsRepository,
            EnergyPlanService energyPlanService
    ) {
        this.systemEnergyDetailsRepository = systemEnergyDetailsRepository;
        this.energyPlanService = energyPlanService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateAndSaveEnergyRevenue(Long systemEnergyDetailsId, long energyDetailsHourDuration) {
        SystemEnergyDetails systemEnergyDetails = systemEnergyDetailsRepository.findById(systemEnergyDetailsId).orElseThrow(() -> {
            String errorMessage = "SystemEnergyDetails with ID [" + systemEnergyDetailsId + "] not found.";
            return new ApplicationException(HttpStatus.BAD_REQUEST, errorMessage);
        });
        energyPlanService.calculateAndSaveEnergyRevenue(systemEnergyDetails, energyDetailsHourDuration);
    }

}
