package com.github.solenra.server.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.solenra.server.model.SolarSystemDto;
import com.github.solenra.server.model.SystemEnergyDetailsDto;
import com.github.solenra.server.model.SystemEnergyDetailsRevenueDto;

import java.security.Principal;
import java.util.Map;

public interface SolarSystemService {

    SolarSystemDto saveSolarSystem(Principal principal, SolarSystemDto solarSystem);

    Page<SolarSystemDto> searchSolarSystems(Principal principal, Long solarSystemId, Pageable pageable);

    Page<SystemEnergyDetailsDto> searchSystemEnergyDetails(Principal principal, Long solarSystemId, Pageable pageable);

    Page<SystemEnergyDetailsRevenueDto> searchSystemEnergyDetailsRevenue(Principal principal, Long solarSystemId, Pageable pageable);

    void runDataLoad(long solarSystemIntegrationId);

    void saveSolarSystemIntegration(Principal principal, Long id, Map<String,String> integrationData);

    void deleteSolarSystemIntegration(Principal principal, Long id, String code);

}
