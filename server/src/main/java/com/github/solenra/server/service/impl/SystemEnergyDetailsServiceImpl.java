package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.integration.SystemDetails;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.SystemDetailsDto;
import com.github.solenra.server.repository.SolarSystemIntegrationRepository;
import com.github.solenra.server.repository.integration.SystemDetailsRepository;
import com.github.solenra.server.repository.integration.SystemEnergyDetailsRepository;
import com.github.solenra.server.service.EnergyPlanService;
import com.github.solenra.server.service.SystemEnergyDetailsService;

import java.time.ZonedDateTime;

@Service("systemEnergyDetailsService")
public class SystemEnergyDetailsServiceImpl implements SystemEnergyDetailsService {

    private final SystemDetailsRepository systemDetailsRepository;
    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SystemEnergyDetailsRepository systemEnergyDetailsRepository;
    private final EnergyPlanService energyPlanService;

    public SystemEnergyDetailsServiceImpl(
            SystemDetailsRepository systemDetailsRepository,
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SystemEnergyDetailsRepository systemEnergyDetailsRepository,
            EnergyPlanService energyPlanService
    ) {
        this.systemDetailsRepository = systemDetailsRepository;
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.systemEnergyDetailsRepository = systemEnergyDetailsRepository;
        this.energyPlanService = energyPlanService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSystemEnergyDetailsNewTransaction(Long solarSystemIntegrationId, SystemEnergyDetails systemEnergyDetails, ZonedDateTime startDate, long energyDetailsMinutesDuration) {
        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        systemEnergyDetails.setSolarSystemIntegration(solarSystemIntegration);
        systemEnergyDetails.setStartDate(startDate);
        systemEnergyDetails.setEndDate(startDate.plusMinutes(energyDetailsMinutesDuration));
        systemEnergyDetails = systemEnergyDetailsRepository.save(systemEnergyDetails);
        energyPlanService.calculateAndSaveEnergyRevenue(systemEnergyDetails, energyDetailsMinutesDuration);

        //systemEnergyDetails.setProcessStatus(SystemEnergyDetails.PROCESS_STATUS_PROCESSED);
        //systemEnergyDetails = systemEnergyDetailsRepository.save(systemEnergyDetails);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SystemDetailsDto saveSystemDetailsNewTransaction(SystemDetails systemDetails, Long solarSystemIntegrationId) {
        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        systemDetails.setSolarSystemIntegration(solarSystemIntegration);
        systemDetails = systemDetailsRepository.save(systemDetails);

        return new SystemDetailsDto(systemDetails);
    }

}
