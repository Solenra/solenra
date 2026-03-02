package com.github.solenra.server.service.impl;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.jobs.SolaredgeLoadJob;
import com.github.solenra.server.jobs.SolaredgeV2LoadJob;
import com.github.solenra.server.model.quartz.SchedulerDto;
import com.github.solenra.server.service.SchedulerService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service("schedulerService")
public class SchedulerServiceImpl implements SchedulerService {

	private final SchedulerFactoryBean mainQuartzScheduler;
	private final SchedulerFactoryBean solaredgeApiQuartzScheduler;
	private final SchedulerFactoryBean solaredgeV2ApiQuartzScheduler;

	public SchedulerServiceImpl(
			SchedulerFactoryBean mainQuartzScheduler,
			@Qualifier("solaredgeApiQuartzScheduler") SchedulerFactoryBean solaredgeApiQuartzScheduler,
			@Qualifier("solaredgeV2ApiQuartzScheduler") SchedulerFactoryBean solaredgeV2ApiQuartzScheduler
	) {
		this.mainQuartzScheduler = mainQuartzScheduler;
		this.solaredgeApiQuartzScheduler = solaredgeApiQuartzScheduler;
		this.solaredgeV2ApiQuartzScheduler = solaredgeV2ApiQuartzScheduler;
	}

	@Override
	public List<SchedulerDto> getSchedulerStatuses(Principal principal) {
		// TODO permission check

		List<SchedulerDto> schedulers = new ArrayList<>();

		try {
			schedulers.add(new SchedulerDto(mainQuartzScheduler.getScheduler()));
			schedulers.add(new SchedulerDto(solaredgeApiQuartzScheduler.getScheduler()));
			schedulers.add(new SchedulerDto(solaredgeV2ApiQuartzScheduler.getScheduler()));
		} catch (SchedulerException e) {
			throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading schedulers", e);
		}

		return schedulers;
	}

	@Override
	public void submitJob(String name, String group, JobDataMap jobDataMap) {
		try {
			switch (name) {
				case SolaredgeLoadJob.NAME:
					solaredgeApiQuartzScheduler.getScheduler().triggerJob(JobKey.jobKey(name, group), jobDataMap);
					break;
				case SolaredgeV2LoadJob.NAME:
					solaredgeV2ApiQuartzScheduler.getScheduler().triggerJob(JobKey.jobKey(name, group), jobDataMap);
					break;
				default:
					mainQuartzScheduler.getScheduler().triggerJob(JobKey.jobKey(name, group), jobDataMap);
					break;
			}
		} catch (SchedulerException e) {
			throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error submitting job", e);
		}
	}

}
