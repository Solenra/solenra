package com.github.solenra.server.controller;

import java.security.Principal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.solenra.server.model.IntegrationDto;
import com.github.solenra.server.service.IntegrationService;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(
            IntegrationService integrationService
    ) {
        this.integrationService = integrationService;
    }

    @RequestMapping("/list")
    public List<IntegrationDto> getIntegrationsList(Principal principal) {
        return integrationService.getIntegrations(principal);
    }

    @PostMapping("")
    public ResponseEntity<?> save(Principal principal, @RequestBody Map<String, Object> integrationData) {
        integrationService.saveIntegration(principal, integrationData);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timezones")
    public List<String> getTimezones(Principal principal) {
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        Collections.sort(zoneIds);
        return zoneIds;
    }

}
