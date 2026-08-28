package com.github.solenra.server.service;

import com.github.solenra.server.entity.integration.SystemEnergyDetails;

public interface CalculationService {

    void calculateAndSaveEnergyRevenue(SystemEnergyDetails systemEnergyDetails, long energyDetailsHourDuration);

}
