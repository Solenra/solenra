package com.github.solenra.server.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.github.solenra.server.service.SolarSystemService;

public class SolaredgeV2LoadJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SolaredgeV2LoadJob.class);

    public static final String NAME = "SolaredgeV2LoadJob";

    private final SolarSystemService solarSystemService;

    public SolaredgeV2LoadJob(SolarSystemService solarSystemService) {
        this.solarSystemService = solarSystemService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.trace("Running SolaredgeV2LoadJob...");
        if (context.getMergedJobDataMap().containsKey("solarSystemIntegrationId")) {
            long solarSystemIntegrationId = context.getMergedJobDataMap().getLong("solarSystemIntegrationId");
            solarSystemService.runDataLoad(solarSystemIntegrationId);
        }
    }

}
