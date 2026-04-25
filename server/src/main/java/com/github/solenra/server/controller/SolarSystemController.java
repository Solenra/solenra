package com.github.solenra.server.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.solenra.server.model.SolarSystemDto;
import com.github.solenra.server.model.SystemEnergyDetailsDto;
import com.github.solenra.server.model.SystemEnergyDetailsRevenueDto;
import com.github.solenra.server.service.SolarSystemService;
import com.github.solenra.server.util.RestUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solar-system")
public class SolarSystemController {

    private final SolarSystemService solarSystemService;

    public SolarSystemController(
            SolarSystemService solarSystemService
    ) {
        this.solarSystemService = solarSystemService;
    }

    @PostMapping("/save")
    public SolarSystemDto save(Principal principal, @RequestBody SolarSystemDto solarSystem) {
        SolarSystemDto savedSolarSystem = solarSystemService.saveSolarSystem(principal, solarSystem);
        return savedSolarSystem;
    }

    @PostMapping("{id}/integration")
    public ResponseEntity<?> saveIntegration(Principal principal, @RequestBody Map<String, String> integrationData, @PathVariable Long id) {
        solarSystemService.saveSolarSystemIntegration(principal, id, integrationData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{id}/integration/{integrationCode}/status")
    public ResponseEntity<?> setSolarSystemIntegrationStatus(Principal principal, @PathVariable Long id, @PathVariable String integrationCode, @RequestBody Map<String, String> statusData) {
        solarSystemService.setSolarSystemIntegrationStatus(principal, id, integrationCode, statusData.get("statusCode"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}/integration/{code}")
    public ResponseEntity<?> deleteIntegration(Principal principal, @PathVariable Long id, @PathVariable String code) {
        solarSystemService.deleteSolarSystemIntegration(principal, id, code);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/search")
    public Page<SolarSystemDto> search(
            Principal principal,
            @RequestParam(required = false) Long solarSystemId,
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

        return solarSystemService.searchSolarSystems(principal, solarSystemId, pageable);
    }

    @RequestMapping("/search-energy-details")
    public Page<SystemEnergyDetailsDto> searchEnergyDetails(
            Principal principal,
            @RequestParam Long solarSystemId,
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

        return solarSystemService.searchSystemEnergyDetails(principal, solarSystemId, pageable);
    }

    @RequestMapping("/search-energy-revenue")
    public Page<SystemEnergyDetailsRevenueDto> searchEnergyRevenue(
            Principal principal,
            @RequestParam Long solarSystemId,
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

        return solarSystemService.searchSystemEnergyDetailsRevenue(principal, solarSystemId, pageable);
    }

}
