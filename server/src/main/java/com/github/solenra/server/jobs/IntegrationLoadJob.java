package com.github.solenra.server.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.github.solenra.server.config.QuartzConfig;
import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.SolarSystemIntegrationStatus;
import com.github.solenra.server.repository.SolarSystemIntegrationRepository;
import com.github.solenra.server.service.SchedulerService;
import com.github.solenra.server.service.TransactionHelperService;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@DisallowConcurrentExecution
public class IntegrationLoadJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationLoadJob.class);

    public static final String NAME = "IntegrationLoadJob";

    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;

    private final SchedulerService schedulerService;
    private final TransactionHelperService transactionHelperService;

    public IntegrationLoadJob(
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SchedulerService schedulerService,
            TransactionHelperService transactionHelperService
    ) {
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.schedulerService = schedulerService;
        this.transactionHelperService = transactionHelperService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.trace("Running IntegrationLoadJob...");

        // find halted processes and change status back to queued
        List<SolarSystemIntegration> solarSystemIntegrationsProcessingHalted = solarSystemIntegrationRepository.findAllByStatusCodeInAndEnabledAndIntegrationEnabledAndProcessingHeartbeatAtLessThan(
                Arrays.asList(SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_QUEUED),
                true,
                true,
                ZonedDateTime.now().minusHours(12) // TODO get from properties
        );
        for (SolarSystemIntegration solarSystemIntegrationProcessingHalted : solarSystemIntegrationsProcessingHalted) {
            logger.debug("Resetting status for SolarSystemIntegration with ID: [{}]", solarSystemIntegrationProcessingHalted.getId());
            transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegrationProcessingHalted.getId(), SolarSystemIntegrationStatus.CODE_PENDING, null);
        }

        List<SolarSystemIntegration> solarSystemIntegrations = solarSystemIntegrationRepository.findAllByStatusCodeInAndEnabledAndIntegrationEnabledAndNextUpdateTimeLessThan(
                Arrays.asList(
                        SolarSystemIntegrationStatus.CODE_UP_TO_DATE,
                        SolarSystemIntegrationStatus.CODE_PENDING,
                        SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_TRANSIENT_ERROR,
 //                       SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_ERROR, // TODO remove this..?
                        SolarSystemIntegrationStatus.CODE_DISABLED

                        // TODO remove this
                        //,SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING
                        /*
                        update SOLAR_SYSTEM_INTEGRATION
                        set SOLAR_SYSTEM_INTEGRATION_STATUS_ID = (
                            select id from SOLAR_SYSTEM_INTEGRATION_STATUS where code = 'pending'
                        )
                        where SOLAR_SYSTEM_INTEGRATION_STATUS_ID in (
                            select id from SOLAR_SYSTEM_INTEGRATION_STATUS where code in (
                                'loading-from-integration-processing',
                                'loading-from-integration-error'
                            )
                        );
                         */
                ),
                true,
                true,
                ZonedDateTime.now()
                //ZonedDateTime.now().plusYears(1)
        );

        if (!solarSystemIntegrations.isEmpty()) {
            logger.debug("Queuing processing for [{}] solar system integrations.", solarSystemIntegrations.size());
        } else {
            logger.trace("Queuing processing for [{}] solar system integrations.", solarSystemIntegrations.size());
        }

        for (SolarSystemIntegration solarSystemIntegration : solarSystemIntegrations) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("solarSystemIntegrationId", solarSystemIntegration.getId());
            if (Integration.CODE_SOLAREDGE_V1.equals(solarSystemIntegration.getIntegration().getCode())) {
                transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegration.getId(), SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_QUEUED, null);
                schedulerService.submitJob(SolaredgeLoadJob.NAME, QuartzConfig.DEFAULT_GROUP, jobDataMap);
            }
            if (Integration.CODE_SOLAREDGE_V2.equals(solarSystemIntegration.getIntegration().getCode())) {
                transactionHelperService.saveSolarSystemIntegrationStatus(solarSystemIntegration.getId(), SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_QUEUED, null);
                schedulerService.submitJob(SolaredgeV2LoadJob.NAME, QuartzConfig.DEFAULT_GROUP, jobDataMap);
            }
        }
    }

}
