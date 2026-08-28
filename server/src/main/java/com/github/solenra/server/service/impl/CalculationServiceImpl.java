package com.github.solenra.server.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.github.solenra.server.entity.EnergyPlanRatePeriod;
import com.github.solenra.server.entity.SystemEnergyDetailsRevenue;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.repository.EnergyPlanRatePeriodRepository;
import com.github.solenra.server.repository.SystemEnergyDetailsRevenueRepository;
import com.github.solenra.server.service.CalculationService;

import jakarta.transaction.Transactional;

@Service("calculationService")
public class CalculationServiceImpl implements CalculationService {

    private final EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository;
    private final SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository;

    public CalculationServiceImpl(
            EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository,
            SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository
    ) {
        this.energyPlanRatePeriodRepository = energyPlanRatePeriodRepository;
        this.systemEnergyDetailsRevenueRepository = systemEnergyDetailsRevenueRepository;
    }

    @Override
    @Transactional
    public void calculateAndSaveEnergyRevenue(SystemEnergyDetails systemEnergyDetails, long energyDetailsMinutesDuration) {

        // TODO function to re-calculate this data for specific plan or all plans
        // TODO support plans that have multiple FIT based on KWh export
        // TODO support batteries
        // TODO support minute rate periods...

        if (systemEnergyDetails == null || systemEnergyDetails.getStartDate() == null || systemEnergyDetails.getStartDate().getDayOfWeek() == null || systemEnergyDetails.getSolarSystemIntegration().getTimezone() == null) {
            return;
        }

        ZoneId targetZone = ZoneId.of(systemEnergyDetails.getSolarSystemIntegration().getTimezone());
        ZonedDateTime startDate = systemEnergyDetails.getStartDate().withZoneSameInstant(targetZone);
        ZonedDateTime endDate = systemEnergyDetails.getEndDate().withZoneSameInstant(targetZone).minusSeconds(1); // adjust by 1 second to ensure correct period is found
        DayOfWeek dayOfWeek = startDate.getDayOfWeek();

        // if period is on one day (period spanning multiple days not supported)
        if (dayOfWeek.equals(endDate.getDayOfWeek())) {
            // convert to UTC to use in SQL where date/time is in UTC
            LocalTime startTimeUtc = startDate.withZoneSameInstant(ZoneId.of("UTC")).toLocalTime();
            LocalTime endTimeUtc = endDate.withZoneSameInstant(ZoneId.of("UTC")).toLocalTime();

            EnergyPlanRatePeriod energyPlanRatePeriod = null;
            try {
                energyPlanRatePeriod = energyPlanRatePeriodRepository.findByEnergyPlanAndEffectiveDates(systemEnergyDetails.getSolarSystemIntegration().getSolarSystem(), startDate, endDate, dayOfWeek, startTimeUtc, endTimeUtc);
            } catch (Exception e) {
                String errorMessage = "Error finding EnergyPlanRatePeriod for systemEnergyDetails ID: [" + systemEnergyDetails.getId() + "], startDate: [" + startDate + "], endDate: [" + endDate + "], dayOfWeek: [" + dayOfWeek + "], startTimeUtc: [" + startTimeUtc + "], endTimeUtc: [" + endTimeUtc + "]";
                throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e);
            }

            if (energyPlanRatePeriod != null) {
                SystemEnergyDetailsRevenue systemEnergyDetailsRevenue = new SystemEnergyDetailsRevenue();
                systemEnergyDetailsRevenue.setSystemEnergyDetails(systemEnergyDetails);
                systemEnergyDetailsRevenue.setEnergyPlanRatePeriod(energyPlanRatePeriod);

                BigDecimal supplyCost = BigDecimal.ZERO;
                BigDecimal importCost = BigDecimal.ZERO;
                BigDecimal exportRevenue = BigDecimal.ZERO;
                BigDecimal selfConsumptionSavings = BigDecimal.ZERO;

                if (energyPlanRatePeriod.getEnergyPlanRate().getEnergyPlan().getSupplyRateValue() != null) {
                    supplyCost = energyPlanRatePeriod.getEnergyPlanRate().getEnergyPlan().getSupplyRateValue()
                            .divide(new BigDecimal(24), 30, RoundingMode.HALF_UP)
                            .divide(new BigDecimal(60), 30, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(energyDetailsMinutesDuration));
                }

                if (systemEnergyDetails.getImportValue() != null && systemEnergyDetails.getImportValue().compareTo(BigDecimal.ZERO) != 0 && energyPlanRatePeriod.getEnergyPlanRate().getRateValue() != null) {
                    // convert Wh to KWh and calculate import cost: Import * Rate value
                    importCost = systemEnergyDetails.getImportValue().divide(new BigDecimal(1000), 30, RoundingMode.HALF_UP).multiply(energyPlanRatePeriod.getEnergyPlanRate().getRateValue());
                }

                if (systemEnergyDetails.getExportValue() != null && systemEnergyDetails.getExportValue().compareTo(BigDecimal.ZERO) != 0 && energyPlanRatePeriod.getEnergyPlanRate().getEnergyPlan().getExportRateValue() != null) {
                    // convert Wh to KWh and calculate export revenue
                    exportRevenue = systemEnergyDetails.getExportValue().divide(new BigDecimal(1000), 30, RoundingMode.HALF_UP).multiply(energyPlanRatePeriod.getEnergyPlanRate().getEnergyPlan().getExportRateValue());
                }

                if (systemEnergyDetails.getSelfConsumptionValue() != null && systemEnergyDetails.getSelfConsumptionValue().compareTo(BigDecimal.ZERO) != 0 && energyPlanRatePeriod.getEnergyPlanRate().getRateValue() != null) {
                    // convert Wh to KWh and calculate self consumption savings
                    selfConsumptionSavings = systemEnergyDetails.getSelfConsumptionValue().divide(new BigDecimal(1000), 30, RoundingMode.HALF_UP).multiply(energyPlanRatePeriod.getEnergyPlanRate().getRateValue());
                }

                systemEnergyDetailsRevenue.setSupplyCost(supplyCost);
                systemEnergyDetailsRevenue.setImportCost(importCost);
                systemEnergyDetailsRevenue.setExportRevenue(exportRevenue);
                systemEnergyDetailsRevenue.setSelfConsumptionSavings(selfConsumptionSavings);

                systemEnergyDetailsRevenue.setCalculationStatus(SystemEnergyDetailsRevenue.CALCULATION_STATUS_PENDING);
                systemEnergyDetailsRevenue = systemEnergyDetailsRevenueRepository.saveAndFlush(systemEnergyDetailsRevenue);
            } else {
                // Rate period not found, nothing to calculate
                // TODO set status, warning and message on systemEnergyDetailsRevenue for manual review in UI
            }
        } else {
            // TODO set status, warning and message, rate period spans multiple days which is not supported in current implementation, manual review in UI required
        }

    }

}
