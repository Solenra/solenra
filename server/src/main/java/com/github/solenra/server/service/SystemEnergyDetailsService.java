package com.github.solenra.server.service;

import java.time.ZonedDateTime;

import com.github.solenra.server.entity.integration.SystemDetails;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.model.SystemDetailsDto;

public interface SystemEnergyDetailsService {

    void saveSystemEnergyDetailsNewTransaction(Long solarSystemIntegrationId, SystemEnergyDetails systemEnergyDetails, ZonedDateTime startDate, long energyDetailsMinutesDuration);

    SystemDetailsDto saveSystemDetailsNewTransaction(SystemDetails systemDetails, Long solarSystemIntegrationId);

}
