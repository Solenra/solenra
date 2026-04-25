package com.github.solenra.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.solenra.server.entity.*;
import com.github.solenra.server.entity.integration.SystemDetails;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.EnergyPlanDto;
import com.github.solenra.server.model.EnergyPlanRateDto;
import com.github.solenra.server.model.EnergyPlanRatePeriodDto;
import com.github.solenra.server.model.EnergyPlanRatePeriodDayDto;
import com.github.solenra.server.model.SolarSystemDto;
import com.github.solenra.server.model.SolarSystemEnergyPlanDto;
import com.github.solenra.server.repository.*;
import com.github.solenra.server.repository.integration.SystemDetailsRepository;
import com.github.solenra.server.service.EnergyPlanService;
import com.github.solenra.server.service.SchedulerService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service("energyPlanService")
public class EnergyPlanServiceImpl implements EnergyPlanService {

    private static final Logger logger = LoggerFactory.getLogger(EnergyPlanServiceImpl.class);

    private final EnergyPlanRepository energyPlanRepository;
    private final EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository;
    private final EnergyPlanStatusRepository energyPlanStatusRepository;
    private final SolarSystemEnergyPlanRepository solarSystemEnergyPlanRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final SolarSystemIntegrationRepository solarSystemIntegrationRepository;
    private final SystemDetailsRepository systemDetailsRepository;
    private final SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository;

    public EnergyPlanServiceImpl(
            SchedulerService schedulerService,
            EnergyPlanRepository energyPlanRepository,
            EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository,
            EnergyPlanStatusRepository energyPlanStatusRepository,
            SolarSystemEnergyPlanRepository solarSystemEnergyPlanRepository,
            SolarSystemRepository solarSystemRepository,
            SolarSystemIntegrationRepository solarSystemIntegrationRepository,
            SystemDetailsRepository systemDetailsRepository,
            SystemEnergyDetailsRevenueRepository systemEnergyDetailsRevenueRepository
    ) {
        this.energyPlanRepository = energyPlanRepository;
        this.energyPlanRatePeriodRepository = energyPlanRatePeriodRepository;
        this.energyPlanStatusRepository = energyPlanStatusRepository;
        this.solarSystemEnergyPlanRepository = solarSystemEnergyPlanRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.solarSystemIntegrationRepository = solarSystemIntegrationRepository;
        this.systemDetailsRepository = systemDetailsRepository;
        this.systemEnergyDetailsRevenueRepository = systemEnergyDetailsRevenueRepository;
    }

    @Override
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEnergyPlanRevenueCalculationNewTransaction(long solarSystemIntegrationId) {
        logger.info("Calculating energy plan revenue for solarSystemIntegrationId: {}", solarSystemIntegrationId);

        SolarSystemIntegration solarSystemIntegration = solarSystemIntegrationRepository.findById(solarSystemIntegrationId).orElseThrow(() -> {
            String errorMessage = "SolarSystemIntegration with ID [" + solarSystemIntegrationId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        SolarSystem solarSystem = solarSystemIntegration.getSolarSystem();
        BigDecimal outlayCost = solarSystem.getOutlayCost();

        ZonedDateTime now = ZonedDateTime.now();

        // TODO could use status field in SUM query to only get new entries instead of recalculating
        // 1 - sum with SQL instead... where status PENDING...
        // 2 - run and save calculations
        // 3 - update status to CALCULATED

        // get energy plans and calculate savings
        List<SolarSystemEnergyPlan> solarSystemEnergyPlans = solarSystemEnergyPlanRepository.findAllBySolarSystemId(solarSystemIntegrationId);

        for (SolarSystemEnergyPlan solarSystemEnergyPlan : solarSystemEnergyPlans) {
            SolarSystemEnergyPlanDto planCumulativeRevenue = systemEnergyDetailsRevenueRepository.sumFieldsBySolarSystemEnergyPlan(
                    solarSystemEnergyPlan.getSolarSystem(), solarSystemEnergyPlan.getEnergyPlan()
            );

            solarSystemEnergyPlan.setCumulativeSupplyCost(planCumulativeRevenue.getCumulativeSupplyCost());
            solarSystemEnergyPlan.setCumulativeImportCost(planCumulativeRevenue.getCumulativeImportCost());
            solarSystemEnergyPlan.setCumulativeExportRevenue(planCumulativeRevenue.getCumulativeExportRevenue());
            solarSystemEnergyPlan.setCumulativeSelfConsumptionSavings(planCumulativeRevenue.getCumulativeSelfConsumptionSavings());

            BigDecimal planCalculatedSavings = planCumulativeRevenue.getCumulativeExportRevenue().add(planCumulativeRevenue.getCumulativeSelfConsumptionSavings());
            solarSystemEnergyPlan.setCalculatedSavings(planCalculatedSavings);

            // calculate ROI for energy plan
            BigDecimal planRoiToDate = null;
            if (outlayCost != null && outlayCost.compareTo(BigDecimal.ZERO) != 0) {
                planRoiToDate = planCalculatedSavings.divide(outlayCost, 30, RoundingMode.HALF_UP);
            }

            ZonedDateTime startDate = solarSystemEnergyPlan.getStartDate();
            ZonedDateTime endDate = solarSystemEnergyPlan.getEndDate();

            if (endDate == null || endDate.isAfter(now)) {
                endDate = now;
            }

            if (startDate != null) {
                long daysBetween = Duration.between(startDate, endDate).toDays();

                if (planRoiToDate != null && daysBetween > 0) {
                    BigDecimal planRoiAnnualised = planRoiToDate
                            .multiply(new BigDecimal(100))
                            .divide(new BigDecimal(daysBetween)
                                            .divide(new BigDecimal(365), 30, RoundingMode.HALF_UP),
                                    30,
                                    RoundingMode.HALF_UP
                            );
                    solarSystemEnergyPlan.setRoiToDate(planRoiToDate);
                    solarSystemEnergyPlan.setRoiAnnualised(planRoiAnnualised);
                }
            }

            solarSystemEnergyPlan = solarSystemEnergyPlanRepository.save(solarSystemEnergyPlan);
        }

        SolarSystemDto cumulativeRevenue = solarSystemEnergyPlanRepository.sumFieldsBySolarSystem(solarSystem);

        solarSystem.setCumulativeSupplyCost(cumulativeRevenue.getCumulativeSupplyCost());
        solarSystem.setCumulativeImportCost(cumulativeRevenue.getCumulativeImportCost());
        solarSystem.setCumulativeExportRevenue(cumulativeRevenue.getCumulativeExportRevenue());
        solarSystem.setCumulativeSelfConsumptionSavings(cumulativeRevenue.getCumulativeSelfConsumptionSavings());

        BigDecimal calculatedSavings = cumulativeRevenue.getCumulativeExportRevenue().add(cumulativeRevenue.getCumulativeSelfConsumptionSavings());
        solarSystem.setCalculatedSavings(calculatedSavings);

        List<SystemDetails> systemDetailsList = systemDetailsRepository.findAllBySolarSystemIntegrationSolarSystemAndInstallationDateNotNull(solarSystem);

        ZonedDateTime installationDate = null;
        for (SystemDetails systemDetails : systemDetailsList) {
            if (systemDetails.getInstallationDate() != null) {
                if (installationDate == null || systemDetails.getInstallationDate().isBefore(installationDate)) {
                    installationDate = systemDetails.getInstallationDate();
                }
            }
        }

        BigDecimal roiToDate = null;
        if (outlayCost != null && outlayCost.compareTo(BigDecimal.ZERO) != 0 && solarSystem.getCalculatedSavings() != null) {
            roiToDate = solarSystem.getCalculatedSavings().divide(outlayCost, 30, RoundingMode.HALF_UP);
        }

        if (installationDate != null) {
            long daysBetween = Duration.between(installationDate, ZonedDateTime.now()).toDays();

            if (roiToDate != null && daysBetween > 0) {

                BigDecimal roiAnnualised = roiToDate
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(daysBetween)
                                .divide(new BigDecimal(365), 30, RoundingMode.HALF_UP),
                                30,
                                RoundingMode.HALF_UP
                        );
                solarSystem.setRoiToDate(roiToDate);
                solarSystem.setRoiAnnualised(roiAnnualised);
                solarSystem = solarSystemRepository.save(solarSystem);
            }

            if (outlayCost != null && outlayCost.compareTo(BigDecimal.ZERO) != 0
                    && calculatedSavings.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal paybackPeriod = outlayCost.divide(
                        calculatedSavings.divide(new BigDecimal(daysBetween), 30, RoundingMode.HALF_UP),
                        3,
                        RoundingMode.HALF_UP
                ).divide(new BigDecimal(365), 30, RoundingMode.HALF_UP);
                solarSystem.setPaybackPeriod(paybackPeriod);

                long daysToBreakEven = paybackPeriod.multiply(new BigDecimal(365)).longValue();
                ZonedDateTime breakEvenDate = installationDate.plusDays(daysToBreakEven);
                solarSystem.setBreakEvenDate(breakEvenDate);
            } else {
                // TODO status for insufficient data
            }
        }


        //recalculateSolarSystem = true;

        /*if (recalculateSolarSystem) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("command", "recalculateSolarSystemRevenue");
            jobDataMap.put("solarSystemId", solarSystem.getId());
            schedulerService.submitJob(CommandRunnerJob.NAME, QuartzConfig.DEFAULT_GROUP, jobDataMap);
        }*/

    }

    @Override
    public void recalculateSolarSystemRevenue(long solarSystemId) {

        SolarSystem solarSystem = solarSystemRepository.findById(solarSystemId).orElseThrow(() -> {
            String errorMessage = "SolarSystem with ID [" + solarSystemId + "] not found.";
            return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        });

        List<SolarSystemEnergyPlan> solarSystemEnergyPlans = solarSystemEnergyPlanRepository.findAllBySolarSystemAndIncludeInRevenueCalculation(solarSystem, true);

        BigDecimal cumulativeSupplyCost = BigDecimal.ZERO;
        BigDecimal cumulativeImportCost = BigDecimal.ZERO;
        BigDecimal cumulativeExportRevenue = BigDecimal.ZERO;
        BigDecimal cumulativeSelfConsumptionSavings = BigDecimal.ZERO;
        BigDecimal cumulativeCalculatedSavings = BigDecimal.ZERO;

        for (SolarSystemEnergyPlan solarSystemEnergyPlan : solarSystemEnergyPlans) {
            cumulativeSupplyCost = cumulativeSupplyCost.add(solarSystemEnergyPlan.getCumulativeSupplyCost());
            cumulativeImportCost = cumulativeImportCost.add(solarSystemEnergyPlan.getCumulativeImportCost());
            cumulativeExportRevenue = cumulativeExportRevenue.add(solarSystemEnergyPlan.getCumulativeExportRevenue());
            cumulativeSelfConsumptionSavings = cumulativeSelfConsumptionSavings.add(solarSystemEnergyPlan.getCumulativeSelfConsumptionSavings());
            //calculatedSavings = ...
        }

        solarSystem.setCumulativeSupplyCost(cumulativeSupplyCost);
        solarSystem.setCumulativeImportCost(cumulativeImportCost);
        solarSystem.setCumulativeExportRevenue(cumulativeExportRevenue);
        solarSystem.setCumulativeSelfConsumptionSavings(cumulativeSelfConsumptionSavings);
        //solarSystem.setCalculatedSavings(calculatedSavings);

        solarSystem.setCalculatedAt(ZonedDateTime.now());

        List<SystemDetails> systemDetailsList = systemDetailsRepository.findAllBySolarSystemIntegrationSolarSystemAndInstallationDateNotNull(solarSystem);
        ZonedDateTime installationDate = null;
        for (SystemDetails systemDetails : systemDetailsList) {
            if (systemDetails.getInstallationDate() != null) {
                if (installationDate == null || systemDetails.getInstallationDate().isBefore(installationDate)) {
                    installationDate = systemDetails.getInstallationDate();
                }
            }
        }

        BigDecimal outlayCost = solarSystem.getOutlayCost();
        BigDecimal roiToDate = null;
        if (outlayCost != null && outlayCost.compareTo(BigDecimal.ZERO) != 0) {
            roiToDate = cumulativeCalculatedSavings.divide(outlayCost, 30, RoundingMode.HALF_UP);
            solarSystem.setRoiToDate(roiToDate);
        }

    }

    @Override
    public EnergyPlanDto getEnergyPlan(Long id) {
        // TODO permission check
        return new EnergyPlanDto(energyPlanRepository.findById(id).orElseThrow(() -> {
            String errorMessage = "EnergyPlan with ID [" + id + "] not found.";
            return new ApplicationException(HttpStatus.BAD_REQUEST, errorMessage);
        }));
    }

    @Override
    public EnergyPlanDto saveEnergyPlan(EnergyPlanDto energyPlanDto) {
        // TODO permission check
        EnergyPlan energyPlan = null;

        String name = energyPlanDto.getName();

        if (energyPlanDto.getId() != null) {
            // Load existing energy plan
            energyPlan = energyPlanRepository.findById(energyPlanDto.getId()).orElse(null);
        }

        if (energyPlan != null) {
            // Clear existing child collections, to be reloaded
            if (energyPlan.getEnergyPlanRates() != null) {
                energyPlan.getEnergyPlanRates().clear();
            }
        } else {
            // Create new energy plan
            energyPlan = new EnergyPlan();

            if (energyPlanRepository.existsByName(name)) {
                name = name + " (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")";
            }
        }

        // Map DTO to entity
        energyPlan.setName(name);
        energyPlan.setNotes(energyPlanDto.getNotes());
        energyPlan.setShared(energyPlanDto.getShared());
        energyPlan.setSupplyRateValue(energyPlanDto.getSupplyRateValue());
        energyPlan.setExportRateValue(energyPlanDto.getExportRateValue());

        // Set status - default to draft if not specified
        if (energyPlanDto.getStatus() != null && energyPlanDto.getStatus().getId() != null) {
            EnergyPlanStatus status = energyPlanStatusRepository.findById(energyPlanDto.getStatus().getId()).orElseThrow(() -> {
                String errorMessage = "EnergyPlanStatus with ID [" + energyPlanDto.getStatus().getId() + "] not found.";
                return new ApplicationException(HttpStatus.BAD_REQUEST, errorMessage);
            });
            energyPlan.setStatus(status);
        } else {
            // Default to draft status
            energyPlan.setStatus(energyPlanStatusRepository.findByCode(EnergyPlanStatus.CODE_DRAFT));
        }

        // Handle energy plan rates
        if (energyPlanDto.getEnergyPlanRates() != null) {
            if (energyPlan.getEnergyPlanRates() == null) {
                energyPlan.setEnergyPlanRates(new ArrayList<>());
            }

            for (EnergyPlanRateDto rateDto : energyPlanDto.getEnergyPlanRates()) {
                EnergyPlanRate rate = new EnergyPlanRate();
                rate.setEnergyPlan(energyPlan);
                rate.setRateName(rateDto.getRateName());
                rate.setRateValue(rateDto.getRateValue());
                rate.setComparativeRateValue(rateDto.getComparativeRateValue());

                energyPlan.getEnergyPlanRates().add(rate);

                // Handle periods
                if (rateDto.getEnergyPlanRatePeriods() != null) {
                    if (rate.getEnergyPlanRatePeriods() == null) {
                        rate.setEnergyPlanRatePeriods(new ArrayList<>());
                    }

                    for (EnergyPlanRatePeriodDto periodDto : rateDto.getEnergyPlanRatePeriods()) {
                        EnergyPlanRatePeriod period = new EnergyPlanRatePeriod();
                        period.setEnergyPlanRate(rate);
                        period.setStartTime(periodDto.getStartTime());
                        period.setEndTime(periodDto.getEndTime());

                        rate.getEnergyPlanRatePeriods().add(period);

                        // Handle days of week
                        if (periodDto.getDaysOfWeek() != null) {
                            if (period.getDaysOfWeek() == null) {
                                period.setDaysOfWeek(new ArrayList<>());
                            }

                            for (EnergyPlanRatePeriodDayDto dayDto : periodDto.getDaysOfWeek()) {
                                EnergyPlanRatePeriodDay day = new EnergyPlanRatePeriodDay();
                                day.setEnergyPlanRatePeriod(period);
                                day.setDayOfWeek(dayDto.getDayOfWeek());

                                period.getDaysOfWeek().add(day);
                            }
                        }
                    }
                }
            }
        }

        // Save the energy plan and child records
        energyPlan = energyPlanRepository.save(energyPlan);

        return new EnergyPlanDto(energyPlan);
    }

    @Override
    public void deleteEnergyPlan(Long id) {
        // TODO permission check
        energyPlanRepository.deleteById(id);
    }

    @Override
    public Page<EnergyPlanDto> searchEnergyPlans(Principal principal, Long energyPlanId, Pageable pageable) {
        // TODO limit to user plans or shared plans...
        Page<EnergyPlan> energyPlanPage = null;
        if (energyPlanId != null) {
            energyPlanPage = new PageImpl<>(
                    Collections.singletonList(
                            energyPlanRepository.findById(energyPlanId).orElseThrow(() -> {
                                String errorMessage = "EnergyPlan with ID [" + energyPlanId + "] not found.";
                                return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
                            })
                    )
            );
        } else {
            energyPlanPage = energyPlanRepository.findAll(pageable);
        }

        // convert the page of domain objects to DTO objects
        return energyPlanPage.map(new Function<EnergyPlan, EnergyPlanDto>() {
            @Override
            public EnergyPlanDto apply(EnergyPlan energyPlan) {
                return new EnergyPlanDto(energyPlan);
            }
        });
    }

}
