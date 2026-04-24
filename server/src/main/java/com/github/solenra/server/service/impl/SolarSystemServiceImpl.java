package com.github.solenra.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.*;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.SolarSystemDto;
import com.github.solenra.server.model.SystemEnergyDetailsDto;
import com.github.solenra.server.model.SystemEnergyDetailsRevenueDto;
import com.github.solenra.server.repository.*;
import com.github.solenra.server.repository.integration.SystemDetailsRepository;
import com.github.solenra.server.repository.integration.SystemEnergyDetailsRepository;
import com.github.solenra.server.service.EnergyPlanService;
import com.github.solenra.server.service.SolarSystemService;
import com.github.solenra.server.service.SolaredgeApiService;
import com.github.solenra.server.service.TransactionHelperService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

@Transactional
@Service("solarSystemService")
public class SolarSystemServiceImpl implements SolarSystemService {

    private static final Logger logger = LoggerFactory.getLogger(SolarSystemServiceImpl.class);

    private final SolaredgeApiService solaredgeApiService;
    private final EnergyPlanService energyPlanService;
    private final TransactionHelperService transactionHelperService;
    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final IntegrationRepository integrationRepository;
    private final SystemDetailsRepository systemDetailsRepository;
    private final SystemEnergyDetailsRepository systemEnergyDetailsRepository;
    private final SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository;
    private final SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository;

    public SolarSystemServiceImpl(
            SolaredgeApiService solaredgeApiService,
            EnergyPlanService energyPlanService,
            TransactionHelperService transactionHelperService,
            SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository,
            SolarSystemRepository solarSystemRepository,
            IntegrationRepository integrationRepository,
            SystemDetailsRepository systemDetailsRepository,
            SystemEnergyDetailsRepository systemEnergyDetailsRepository,
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository,
            SolarSystemIntegrationAuthCredentialRepository solarSystemIntegrationAuthCredentialRepository
    ) {
        this.solaredgeApiService = solaredgeApiService;
        this.energyPlanService = energyPlanService;
        this.transactionHelperService = transactionHelperService;
        this.systemEnergyDetailsRevenueRepository = systemEnergyDetailsRevenueRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.integrationRepository = integrationRepository;
        this.systemDetailsRepository = systemDetailsRepository;
        this.systemEnergyDetailsRepository = systemEnergyDetailsRepository;
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.solarSystemIntegrationStatusRepository = solarSystemIntegrationStatusRepository;
        this.solarSystemIntegrationAuthCredentialRepository = solarSystemIntegrationAuthCredentialRepository;
    }

    private SolarSystem getSolarSystem(Long id) {
        return solarSystemRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "SolarSystem with ID [" + id + "] not found.";
            return new ApplicationException(HttpStatus.BAD_REQUEST, errorMessage);
        });
    }

    private boolean bigDecimalsChanged(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null && newValue == null) return false;
        if (oldValue == null || newValue == null) return true;
        return oldValue.compareTo(newValue) != 0;
    }

    @Override
    public SolarSystemDto saveSolarSystem(Principal principal, SolarSystemDto solarSystemDto) {
        // TODO permission check
        // TODO do not save duplicate integration details, reuse

        // update SOLAR_SYSTEM_INTEGRATION set SOLAR_SYSTEM_INTEGRATION_STATUS_ID = (select id from SOLAR_SYSTEM_INTEGRATION_STATUS where code = 'pending');

        SolarSystem solarSystem = null;

        boolean recalculate = false;

        if (solarSystemDto.getId() != null) {
            solarSystem = getSolarSystem(solarSystemDto.getId());
            if (bigDecimalsChanged(solarSystem.getOutlayCost(), solarSystemDto.getOutlayCost())) {
                recalculate = true;
            }
            solarSystem = SolarSystem.convertToEntity(solarSystemDto, solarSystem);
        } else {
            solarSystem = SolarSystem.convertToEntity(solarSystemDto);
        }

        solarSystem = solarSystemRepository.save(solarSystem);

        // TODO create user association if necessary

        if (recalculate) {
            // TODO set CALCULATED revenue to PENDING, submit data load job for all integrations for this solar system, which will trigger revenue re-calculation when complete
        }

        return new SolarSystemDto(solarSystem);
    }

    @Override
    public Page<SolarSystemDto> searchSolarSystems(Principal principal, Long solarSystemId, Pageable pageable) {
        // TODO limit to user
        Page<SolarSystem> solarSystemPage = null;
        if (solarSystemId != null) {
            solarSystemPage = new PageImpl<>(
                    Collections.singletonList(
                        getSolarSystem(solarSystemId)
                    )
            );
        } else {
            solarSystemPage = solarSystemRepository.findAll(pageable);
        }

        // convert the page of domain objects to DTO objects
        return solarSystemPage.map(new Function<SolarSystem, SolarSystemDto>() {
            @Override
            public SolarSystemDto apply(SolarSystem solarSystem) {
                Map<Long, ZonedDateTime> latestLoadedDataForIntegrations = new HashMap<>();

                if (solarSystem.getSolarSystemIntegrations() != null) {
                    for (SolarSystemIntegration solarSystemIntegration : solarSystem.getSolarSystemIntegrations()) {
                        if (SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING.equals(solarSystemIntegration.getStatus().getCode())) {
                            SystemEnergyDetails maxSystemEnergyDetails = systemEnergyDetailsRepository.findFirstBySolarSystemIntegrationOrderByEndDateDesc(solarSystemIntegration);
                            if (maxSystemEnergyDetails != null) {
                                latestLoadedDataForIntegrations.put(solarSystemIntegration.getId(), maxSystemEnergyDetails.getEndDate()/*.atZone(ZoneId.of("UTC"))*/);
                            }
                        }
                    }
                }

                return new SolarSystemDto(solarSystem, latestLoadedDataForIntegrations);
            }
        });
    }

    @Override
    public Page<SystemEnergyDetailsDto> searchSystemEnergyDetails(Principal principal, Long solarSystemId, Pageable pageable) {
        Page<SystemEnergyDetails> systemEnergyDetailsPage = systemEnergyDetailsRepository.findAllBySolarSystemIntegrationSolarSystemId(solarSystemId, pageable); // TODO limit to user

        // convert the page of domain objects to DTO objects
        return systemEnergyDetailsPage.map(SystemEnergyDetailsDto::new);
    }

    @Override
    public Page<SystemEnergyDetailsRevenueDto> searchSystemEnergyDetailsRevenue(Principal principal, Long solarSystemId, Pageable pageable) {
        Page<SystemEnergyDetailsRevenue> systemEnergyDetailsRevenuePage = systemEnergyDetailsRevenueRepository.findAllBySystemEnergyDetailsSolarSystemIntegrationSolarSystemId(solarSystemId, pageable); // TODO limit to user

        // convert the page of domain objects to DTO objects
        return systemEnergyDetailsRevenuePage.map(SystemEnergyDetailsRevenueDto::new);
    }

    @Override
    public void runDataLoad(long solarSystemIntegrationId) {
        logger.debug("Run data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);

        try {
            SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
                String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
                return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
            });

            ZonedDateTime nextUpdateTime = ZonedDateTime.now().plusHours(1);

            if (Boolean.TRUE.equals(solarSystemIntegration.getEnabled())
                    && Boolean.TRUE.equals(solarSystemIntegration.getIntegration().getEnabled())
            ) {
                transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING, null);

                logger.debug("Running data load for solarSystemIntegrationId [{}]", solarSystemIntegrationId);

                try {
                    switch (solarSystemIntegration.getIntegration().getCode()) {
                        case Integration.CODE_SOLAREDGE_V1:
                            solaredgeApiService.runDataLoad(solarSystemIntegrationId);
                            break;
                        case Integration.CODE_SOLAREDGE_V2:
                            solaredgeApiService.runDataLoadV2(solarSystemIntegrationId);
                            break;

                    }


                    energyPlanService.updateEnergyPlanRevenueCalculationNewTransaction(solarSystemIntegrationId);

                    transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, SolarSystemIntegrationStatus.CODE_UP_TO_DATE, nextUpdateTime);

                } catch (ExhaustedRetryException e) {
                    logger.error("ExhaustedRetryException Error running data load for solarSystemIntegrationId [" + solarSystemIntegrationId + "]", e);
                    transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_TRANSIENT_ERROR, nextUpdateTime);
                } catch (Exception e) {
                    // TODO set user message somewhere of cause...

                    logger.error("Error running data load for solarSystemIntegrationId [" + solarSystemIntegrationId + "]", e);

                    String statusCode = SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_ERROR;

                    // check if cause is expired credentials and set specific status
                    Throwable t = e;
                    while (t != null) {
                        if (CredentialsExpiredException.class.isInstance(t)) {
                            statusCode = SolarSystemIntegrationStatus.CODE_EXPIRED_CREDENTIALS;
                            break;
                        }
                        t = t.getCause();
                    }

                    transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, statusCode, nextUpdateTime);

                    if (SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_ERROR.equals(statusCode)) {
                        // TODO show error message to user, with retry button which sets status back to pending
                    }

                }

            } else {
                transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationId, SolarSystemIntegrationStatus.CODE_DISABLED, null);
            }

        } catch (Exception e) {
            // TODO re-load object and set status to transient error
        }

    }

    @Override
    public void saveSolarSystemIntegration(Principal principal, Long id, Map<String, String> integrationData) {
        // TODO permission check

        SolarSystem solarSystem = getSolarSystem(id);

        if (integrationData == null || integrationData.get("code") == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration code is required.");
        }

        Integration integration = integrationRepository.findByCode(integrationData.get("code"));

        if (integration == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration with code [" + integrationData.get("code") + "] not found.");
        }

        boolean exists = solarSystemIntegrationRepository.existsBySolarSystemAndIntegration(solarSystem, integration);

        if (exists) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration with code [" + integrationData.get("code") + "] already exists for solar system with ID [" + id + "].");
        }

        String initialStatusCode = SolarSystemIntegrationStatus.CODE_PENDING;
        if (Integration.CODE_SOLAREDGE_V2.equals(integration.getCode())) {
            initialStatusCode = SolarSystemIntegrationStatus.CODE_SETUP;
        }

        SolarSystemIntegration solarSystemIntegration = new SolarSystemIntegration();
        solarSystemIntegration.setSolarSystem(solarSystem);
        solarSystemIntegration.setIntegration(integration);
        solarSystemIntegration.setEnabled(true);
        solarSystemIntegration.setStatus(solarSystemIntegrationStatusRepository.findByCode(initialStatusCode));

        if (integrationData.containsKey("timezone")) {
            solarSystemIntegration.setTimezone(integrationData.get("timezone"));
        }

        // Save the new integration
        solarSystemIntegration = solarSystemIntegrationRepository.save(solarSystemIntegration);

        // Save additional integration details from integrationData map
        if (Integration.CODE_SOLAREDGE_V1.equals(integration.getCode())) {
            String systemId = integrationData.get("system-id");
            if (systemId != null && !systemId.trim().isEmpty()) {
                SolarSystemIntegrationAuthCredential credential = new SolarSystemIntegrationAuthCredential();
                credential.setSolarSystemIntegration(solarSystemIntegration);
                credential.setType(SolarSystemIntegrationAuthCredential.TYPE_SYSTEM_ID);
                credential.setValue(systemId.trim());
                solarSystemIntegrationAuthCredentialRepository.save(credential);
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "System ID is required for SolarEdge integration.");
            }

            String apiKey = integrationData.get("api-key");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                SolarSystemIntegrationAuthCredential credential = new SolarSystemIntegrationAuthCredential();
                credential.setSolarSystemIntegration(solarSystemIntegration);
                credential.setType(SolarSystemIntegrationAuthCredential.TYPE_API_KEY);
                credential.setValue(apiKey.trim());
                solarSystemIntegrationAuthCredentialRepository.save(credential);
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "API key is required for SolarEdge integration.");
            }
        }

    }

    @Override
    public void deleteSolarSystemIntegration(Principal principal, Long id, String code) {
        // TODO permission check

        // TODO check/wait for any running data load jobs for this integration and delay deletion if any running, or cancel them

        SolarSystem solarSystem = getSolarSystem(id);
        Integration integration = integrationRepository.findByCode(code);

        if (integration == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration with code [" + code + "] not found.");
        }

        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findBySolarSystemAndIntegration(solarSystem, integration);

        if (solarSystemIntegration == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Integration with code [" + code + "] not found for solar system with ID [" + id + "].");
        }

        // Delete associated credentials first
        solarSystemIntegrationAuthCredentialRepository.deleteAllBySolarSystemIntegrationId(solarSystemIntegration.getId());

        // Delete any integration-specific metadata before removing the integration
        systemDetailsRepository.deleteAllBySolarSystemIntegrationId(solarSystemIntegration.getId());
        systemEnergyDetailsRevenueRepository.deleteAllBySystemEnergyDetailsSolarSystemIntegrationId(solarSystemIntegration.getId());
        systemEnergyDetailsRepository.deleteAllBySolarSystemIntegrationId(solarSystemIntegration.getId());

        // Delete the integration
        solarSystemIntegrationRepository.delete(solarSystemIntegration);
    }

}
