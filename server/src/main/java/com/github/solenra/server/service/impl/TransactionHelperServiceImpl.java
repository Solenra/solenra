package com.github.solenra.server.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.SolarSystemIntegrationAuthCredential;
import com.github.solenra.server.entity.SolarSystemIntegrationStatus;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.repository.SolarSystemIntegrationAuthCredentialRepository;
import com.github.solenra.server.repository.SolarSystemIntegrationRepository;
import com.github.solenra.server.repository.SolarSystemIntegrationStatusRepository;
import com.github.solenra.server.service.TransactionHelperService;

import java.time.ZonedDateTime;

@Service("transactionHelperService")
public class TransactionHelperServiceImpl implements TransactionHelperService {

    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository;
    private final SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository;

    public TransactionHelperServiceImpl(
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository,
            SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository
    ) {
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.solarSystemIntegrationStatusRepository = solarSystemIntegrationStatusRepository;
        this.solarSystemIntegrationAuthCredentialRepository = solarSystemIntegrationAuthCredentialRepository;
    }

    private SolarSystemIntegration getSolarSystemIntegration(Long id) {
        return solarSystemIntegrationRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + id + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSolarSystemIntegrationStatus(Long solarSystemIntegrationId, String statusCode, ZonedDateTime nextUpdateTime) {
        SolarSystemIntegration solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);

        if (SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING.equals(statusCode)) {
            solarSystemIntegration.setProcessingStartedAt(ZonedDateTime.now());
            solarSystemIntegration.setProcessingHeartbeatAt(ZonedDateTime.now());
        }

        if (nextUpdateTime != null) {
            solarSystemIntegration.setNextUpdateTime(nextUpdateTime);
        }

        SolarSystemIntegrationStatus status = solarSystemIntegrationStatusRepository.findByCode(statusCode);
        if (status == null) {
            String errorMessage = "SolarSystemIntegrationStatus with code [" + statusCode + "] not found.";
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }

        solarSystemIntegration.setStatus(status);
        solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSolarSystemIntegrationProcessingHeartbeat(Long solarSystemIntegrationId, ZonedDateTime heartbeat) {
        SolarSystemIntegration solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);

        solarSystemIntegration.setProcessingHeartbeatAt(heartbeat);
        solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIntegrationAuthCredential(Long solarSystemIntegrationId, String type, String value) {
        SolarSystemIntegration solarSystemIntegration = null;
        SolarSystemIntegrationAuthCredential credential = solarSystemIntegrationAuthCredentialRepository.findBySolarSystemIntegrationIdAndType(solarSystemIntegrationId, type);

        if (credential == null) {
            solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);
            credential = new SolarSystemIntegrationAuthCredential();
            credential.setSolarSystemIntegration(solarSystemIntegration);
            credential.setType(type);
        }

        credential.setValue(value);

        credential = solarSystemIntegrationAuthCredentialRepository.save(credential);

        if (SolarSystemIntegrationAuthCredential.TYPE_AUTH_CODE.equals(type)) {
            // enable the integration now it is connected
            if (solarSystemIntegration == null) {
                solarSystemIntegration = getSolarSystemIntegration(solarSystemIntegrationId);
            }

            solarSystemIntegration.setEnabled(true);
            solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);
        }
    }

}
