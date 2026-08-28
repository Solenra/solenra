package com.github.solenra.server.service;

public interface CalculationService {

    void calculateAndSaveEnergyRevenue(Long systemEnergyDetailsId, long energyDetailsHourDuration);

}
