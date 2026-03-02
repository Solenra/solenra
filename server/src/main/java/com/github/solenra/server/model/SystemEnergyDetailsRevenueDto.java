package com.github.solenra.server.model;

import java.math.BigDecimal;

import com.github.solenra.server.entity.SystemEnergyDetailsRevenue;

public class SystemEnergyDetailsRevenueDto {

    private Long id;
    private String calculationStatus;
    private SystemEnergyDetailsDto systemEnergyDetails;
    private EnergyPlanRatePeriodDto energyPlanRatePeriod;
    private BigDecimal supplyCost;
    private BigDecimal importCost;
    private BigDecimal exportRevenue;
    private BigDecimal selfConsumptionSavings;

    public SystemEnergyDetailsRevenueDto() {
    }

    public SystemEnergyDetailsRevenueDto(SystemEnergyDetailsRevenue systemEnergyDetailsRevenue) {
        this(systemEnergyDetailsRevenue, true);
    }

    public SystemEnergyDetailsRevenueDto(SystemEnergyDetailsRevenue systemEnergyDetailsRevenue, boolean setSystemEnergyDetails) {
        if (systemEnergyDetailsRevenue != null) {
            this.id = systemEnergyDetailsRevenue.getId();
            this.energyPlanRatePeriod = new EnergyPlanRatePeriodDto(systemEnergyDetailsRevenue.getEnergyPlanRatePeriod());
            this.supplyCost = systemEnergyDetailsRevenue.getSupplyCost();
            this.importCost = systemEnergyDetailsRevenue.getImportCost();
            this.exportRevenue = systemEnergyDetailsRevenue.getExportRevenue();
            this.selfConsumptionSavings = systemEnergyDetailsRevenue.getSelfConsumptionSavings();

            if (setSystemEnergyDetails) {
                this.systemEnergyDetails = new SystemEnergyDetailsDto(systemEnergyDetailsRevenue.getSystemEnergyDetails());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCalculationStatus() {
        return calculationStatus;
    }

    public void setCalculationStatus(String calculationStatus) {
        this.calculationStatus = calculationStatus;
    }

    public SystemEnergyDetailsDto getSystemEnergyDetails() {
        return systemEnergyDetails;
    }

    public void setSystemEnergyDetails(SystemEnergyDetailsDto systemEnergyDetails) {
        this.systemEnergyDetails = systemEnergyDetails;
    }

    public EnergyPlanRatePeriodDto getEnergyPlanRatePeriod() {
        return energyPlanRatePeriod;
    }

    public void setEnergyPlanRatePeriod(EnergyPlanRatePeriodDto energyPlanRatePeriod) {
        this.energyPlanRatePeriod = energyPlanRatePeriod;
    }

    public BigDecimal getSupplyCost() {
        return supplyCost;
    }

    public void setSupplyCost(BigDecimal supplyCost) {
        this.supplyCost = supplyCost;
    }

    public BigDecimal getImportCost() {
        return importCost;
    }

    public void setImportCost(BigDecimal importCost) {
        this.importCost = importCost;
    }

    public BigDecimal getExportRevenue() {
        return exportRevenue;
    }

    public void setExportRevenue(BigDecimal exportRevenue) {
        this.exportRevenue = exportRevenue;
    }

    public BigDecimal getSelfConsumptionSavings() {
        return selfConsumptionSavings;
    }

    public void setSelfConsumptionSavings(BigDecimal selfConsumptionSavings) {
        this.selfConsumptionSavings = selfConsumptionSavings;
    }

}
