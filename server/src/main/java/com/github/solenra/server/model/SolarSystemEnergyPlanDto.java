package com.github.solenra.server.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.github.solenra.server.entity.SolarSystemEnergyPlan;

public class SolarSystemEnergyPlanDto {

    private Long id;

    private EnergyPlanDto energyPlan;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private Boolean includeInRevenueCalculation;

    private BigDecimal cumulativeSupplyCost;

    private BigDecimal cumulativeImportCost;

    private BigDecimal cumulativeExportRevenue;

    private BigDecimal cumulativeSelfConsumptionSavings;

    private BigDecimal calculatedSavings;

    private BigDecimal roiAnnualised;

    private ZonedDateTime calculatedAt;

    public SolarSystemEnergyPlanDto() {
    }

    public SolarSystemEnergyPlanDto(
            BigDecimal cumulativeSupplyCost,
            BigDecimal cumulativeImportCost,
            BigDecimal cumulativeExportRevenue,
            BigDecimal cumulativeSelfConsumptionSavings
    ) {
        this.cumulativeSupplyCost = cumulativeSupplyCost != null ? cumulativeSupplyCost : BigDecimal.ZERO;
        this.cumulativeImportCost = cumulativeImportCost != null ? cumulativeImportCost : BigDecimal.ZERO;
        this.cumulativeExportRevenue = cumulativeExportRevenue != null ? cumulativeExportRevenue : BigDecimal.ZERO;
        this.cumulativeSelfConsumptionSavings = cumulativeSelfConsumptionSavings != null ? cumulativeSelfConsumptionSavings : BigDecimal.ZERO;
    }

    public SolarSystemEnergyPlanDto(SolarSystemEnergyPlan solarSystemEnergyPlan) {
        this.id = solarSystemEnergyPlan.getId();
        this.energyPlan = new EnergyPlanDto(solarSystemEnergyPlan.getEnergyPlan());
        this.startDate = solarSystemEnergyPlan.getStartDate();
        this.endDate = solarSystemEnergyPlan.getEndDate();
        this.includeInRevenueCalculation = solarSystemEnergyPlan.getIncludeInRevenueCalculation();
        this.cumulativeSupplyCost = solarSystemEnergyPlan.getCumulativeSupplyCost();
        this.cumulativeImportCost = solarSystemEnergyPlan.getCumulativeImportCost();
        this.cumulativeExportRevenue = solarSystemEnergyPlan.getCumulativeExportRevenue();
        this.cumulativeSelfConsumptionSavings = solarSystemEnergyPlan.getCumulativeSelfConsumptionSavings();
        this.calculatedSavings = solarSystemEnergyPlan.getCalculatedSavings();
        this.roiAnnualised = solarSystemEnergyPlan.getRoiAnnualised();
        this.calculatedAt = solarSystemEnergyPlan.getCalculatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnergyPlanDto getEnergyPlan() {
        return energyPlan;
    }

    public void setEnergyPlan(EnergyPlanDto energyPlan) {
        this.energyPlan = energyPlan;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getIncludeInRevenueCalculation() {
        return includeInRevenueCalculation;
    }

    public void setIncludeInRevenueCalculation(Boolean includeInRevenueCalculation) {
        this.includeInRevenueCalculation = includeInRevenueCalculation;
    }

    public BigDecimal getCumulativeSupplyCost() {
        return cumulativeSupplyCost;
    }

    public void setCumulativeSupplyCost(BigDecimal cumulativeSupplyCost) {
        this.cumulativeSupplyCost = cumulativeSupplyCost;
    }

    public BigDecimal getCumulativeImportCost() {
        return cumulativeImportCost;
    }

    public void setCumulativeImportCost(BigDecimal cumulativeImportCost) {
        this.cumulativeImportCost = cumulativeImportCost;
    }

    public BigDecimal getCumulativeExportRevenue() {
        return cumulativeExportRevenue;
    }

    public void setCumulativeExportRevenue(BigDecimal cumulativeExportRevenue) {
        this.cumulativeExportRevenue = cumulativeExportRevenue;
    }

    public BigDecimal getCumulativeSelfConsumptionSavings() {
        return cumulativeSelfConsumptionSavings;
    }

    public void setCumulativeSelfConsumptionSavings(BigDecimal cumulativeSelfConsumptionSavings) {
        this.cumulativeSelfConsumptionSavings = cumulativeSelfConsumptionSavings;
    }

    public BigDecimal getCalculatedSavings() {
        return calculatedSavings;
    }

    public void setCalculatedSavings(BigDecimal calculatedSavings) {
        this.calculatedSavings = calculatedSavings;
    }

    public BigDecimal getRoiAnnualised() {
        return roiAnnualised;
    }

    public void setRoiAnnualised(BigDecimal roiAnnualised) {
        this.roiAnnualised = roiAnnualised;
    }

    public ZonedDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(ZonedDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

}
