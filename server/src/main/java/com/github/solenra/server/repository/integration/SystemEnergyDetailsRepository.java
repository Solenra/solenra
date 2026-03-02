package com.github.solenra.server.repository.integration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;

import java.util.List;

public interface SystemEnergyDetailsRepository extends JpaRepository<SystemEnergyDetails, Long> {

    SystemEnergyDetails findFirstBySolarSystemIntegrationOrderByEndDateDesc(SolarSystemIntegration solarSystemIntegration);

    Page<SystemEnergyDetails> findAllBySolarSystemIntegrationSolarSystemId(Long solarSystemId, Pageable pageable);

    List<SystemEnergyDetails> findAllBySolarSystemIntegrationId(Long solarSystemIntegrationId);

    List<SystemEnergyDetails> findAllBySolarSystemIntegrationIdAndSystemEnergyDetailsRevenuesIsNull(Long solarSystemIntegrationId);

}
