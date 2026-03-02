package com.github.solenra.server.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.github.solenra.server.service.SolarSystemService;

public class SolaredgeLoadJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SolaredgeLoadJob.class);

    public static final String NAME = "SolaredgeLoadJob";

    private final SolarSystemService solarSystemService;

    public SolaredgeLoadJob(SolarSystemService solarSystemService) {
        this.solarSystemService = solarSystemService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("Running SolaredgeLoadJob...");
        if (context.getMergedJobDataMap().containsKey("solarSystemIntegrationId")) {
            long solarSystemIntegrationId = context.getMergedJobDataMap().getLong("solarSystemIntegrationId");
            solarSystemService.runDataLoad(solarSystemIntegrationId);
        }
    }

}
