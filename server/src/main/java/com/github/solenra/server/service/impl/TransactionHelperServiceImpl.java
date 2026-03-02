package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.SolarSystemIntegrationStatus;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.repository.SolarSystemIntegrationRepository;
import com.github.solenra.server.repository.SolarSystemIntegrationStatusRepository;
import com.github.solenra.server.service.TransactionHelperService;

import java.time.ZonedDateTime;

@Service("transactionHelperService")
public class TransactionHelperServiceImpl implements TransactionHelperService {

    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository;

    public TransactionHelperServiceImpl(
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository
    ) {
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.solarSystemIntegrationStatusRepository = solarSystemIntegrationStatusRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSolarSystemIntegrationStatus(Long solarSystemIntegrationId, String statusCode, ZonedDateTime nextUpdateTime) {
        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        if (SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING.equals(statusCode)) {
            solarSystemIntegration.setProcessingStartedAt(ZonedDateTime.now());
            solarSystemIntegration.setProcessingHeartbeatAt(ZonedDateTime.now());
        }

        if (nextUpdateTime != null) {
            solarSystemIntegration.setNextUpdateTime(nextUpdateTime);
        }

        solarSystemIntegration.setStatus(solarSystemIntegrationStatusRepository.findByCode(statusCode));
        solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
    }

}
