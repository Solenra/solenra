package com.github.solenra.server.repository.integration;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.SolarSystem;
import com.github.solenra.server.entity.integration.SystemDetails;

import java.util.List;

public interface SystemDetailsRepository extends JpaRepository<SystemDetails, Long> {

    List<SystemDetails> findAllBySolarSystemIntegrationSolarSystemAndInstallationDateNotNull(SolarSystem solarSystem);

    void deleteAllBySolarSystemIntegrationId(Long solarSystemIntegrationId);

}
