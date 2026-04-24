package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.SolarSystem;
import com.github.solenra.server.entity.SolarSystemIntegration;

import java.time.ZonedDateTime;
import java.util.List;

public interface SolarSystemIntegrationRepository extends JpaRepository<SolarSystemIntegration, Long> {

    List<SolarSystemIntegration> findAllByStatusCodeInAndEnabledAndIntegrationEnabledAndNextUpdateTimeLessThan(
            List<String> statusCodes, boolean enabled, boolean integrationEnabled, ZonedDateTime currentTime
    );

    List<SolarSystemIntegration> findAllBySolarSystem(SolarSystem solarSystem);

    List<SolarSystemIntegration> findAllBySolarSystemId(Long solarSystemId);

    SolarSystemIntegration findBySolarSystemAndIntegration(SolarSystem solarSystem, Integration integration);

    List<SolarSystemIntegration> findAllByStatusCodeInAndEnabledAndIntegrationEnabledAndProcessingHeartbeatAtLessThan(
            List<String> statusCodes, boolean enabled, boolean integrationEnabled, ZonedDateTime heartbeatTimeout);

    boolean existsBySolarSystemAndIntegration(SolarSystem solarSystem, Integration integration);

}
