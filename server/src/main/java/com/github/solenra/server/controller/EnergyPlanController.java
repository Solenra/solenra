package com.github.solenra.server.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.solenra.server.model.EnergyPlanDto;
import com.github.solenra.server.service.EnergyPlanService;
import com.github.solenra.server.util.RestUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/energy-plan")
public class EnergyPlanController {

    private final EnergyPlanService energyPlanService;

    public EnergyPlanController(EnergyPlanService energyPlanService) {
        this.energyPlanService = energyPlanService;
    }

    @RequestMapping("/search")
    public Page<EnergyPlanDto> search(
            Principal principal,
            @RequestParam(required = false) Long energyPlanId,
            @RequestParam(defaultValue = "0") Integer pageIndex,
            @RequestParam(defaultValue = "25") Integer pageSize,
            @RequestParam(required = false) List<String> sort)
    {
        // determine the columns and sort order
        if (CollectionUtils.isEmpty(sort)) {
            // set default sort
            sort = Collections.singletonList("name:asc");
        }

        List<Sort.Order> orders = RestUtils.getSortOrder(sort);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, orders.isEmpty() ? Sort.unsorted() : Sort.by(orders));

        return energyPlanService.searchEnergyPlans(principal, energyPlanId, pageable);
    }

    @RequestMapping("/get")
    public EnergyPlanDto get(@RequestParam Long id) {
        // TODO permission check
        return energyPlanService.getEnergyPlan(id);
    }

    @PostMapping("/save")
    public EnergyPlanDto save(@RequestBody EnergyPlanDto energyPlan) {
        // TODO permission check
        return energyPlanService.saveEnergyPlan(energyPlan);
    }

    @PostMapping("/delete")
    public void delete(@RequestParam Long id) {
        // TODO permission check
        energyPlanService.deleteEnergyPlan(id);
    }

}
