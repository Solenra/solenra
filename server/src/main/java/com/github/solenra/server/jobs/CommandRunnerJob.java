package com.github.solenra.server.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.github.solenra.server.service.EnergyPlanService;

@DisallowConcurrentExecution
public class CommandRunnerJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(CommandRunnerJob.class);

    public static final String NAME = "CommandRunnerJob";

    private final EnergyPlanService energyPlanService;

    public CommandRunnerJob(EnergyPlanService energyPlanService) {
        this.energyPlanService = energyPlanService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String command = context.getMergedJobDataMap().getString("command");
        logger.info("Running CommandRunnerJob with command: {}", command);
        if (command != null) {
            if (command.equals("recalculateSolarSystemRevenue")) {
                long solarSystemId = context.getMergedJobDataMap().getLong("solarSystemId");
                energyPlanService.recalculateSolarSystemRevenue(solarSystemId);
            }
        }
    }

}
