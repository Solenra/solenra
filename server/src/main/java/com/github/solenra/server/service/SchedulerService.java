package com.github.solenra.server.service;

import org.quartz.JobDataMap;

import com.github.solenra.server.model.quartz.SchedulerDto;

import java.security.Principal;
import java.util.List;

public interface SchedulerService {

	List<SchedulerDto> getSchedulerStatuses(Principal principal);

	void submitJob(String name, String group, JobDataMap jobDataMap);

}
