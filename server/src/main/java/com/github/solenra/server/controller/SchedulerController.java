package com.github.solenra.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.solenra.server.model.quartz.SchedulerDto;
import com.github.solenra.server.service.SchedulerService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/scheduler")
public class SchedulerController {

	private final SchedulerService schedulerService;

	public SchedulerController(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	@GetMapping("/status")
	public List<SchedulerDto> states(Principal principal) {
		return schedulerService.getSchedulerStatuses(principal);
	}

}
