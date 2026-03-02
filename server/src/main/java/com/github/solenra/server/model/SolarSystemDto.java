package com.github.solenra.server.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.solenra.server.entity.SolarSystem;
import com.github.solenra.server.entity.SolarSystemIntegration;

public class SolarSystemDto {

    private Long id;
    private String name;
    private String timezone;
    private String notes;
    private BigDecimal outlayCost;
    private BigDecimal cumulativeSupplyCost;
    private BigDecimal cumulativeImportCost;
    private BigDecimal cumulativeExportRevenue;
    private BigDecimal cumulativeSelfConsumptionSavings;
    private BigDecimal calculatedSavings;
    private ZonedDateTime calculatedAt;
    private BigDecimal roiToDate;
    private BigDecimal roiAnnualised;
    private BigDecimal paybackPeriod;
    private ZonedDateTime breakEvenDate;
    private List<SolarSystemEnergyPlanDto> energyPlans;
    private List<SolarSystemIntegrationDto> solarSystemIntegrations;

    public SolarSystemDto() {
    }

    public SolarSystemDto(
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

    public SolarSystemDto(SolarSystem solarSystem) {
        this(solarSystem, new HashMap<>());
    }

    public SolarSystemDto(SolarSystem solarSystem, Map<Long, ZonedDateTime> latestLoadedDataForIntegrations) {
        if (solarSystem != null) {
            this.id = solarSystem.getId();
            this.name = solarSystem.getName();
            this.timezone = solarSystem.getTimezone();
            this.notes = solarSystem.getNotes();
            this.outlayCost = solarSystem.getOutlayCost();
            this.cumulativeSupplyCost = solarSystem.getCumulativeSupplyCost();
            this.cumulativeImportCost = solarSystem.getCumulativeImportCost();
            this.cumulativeExportRevenue = solarSystem.getCumulativeExportRevenue();
            this.cumulativeSelfConsumptionSavings = solarSystem.getCumulativeSelfConsumptionSavings();
            this.calculatedSavings = solarSystem.getCalculatedSavings();
            this.calculatedAt = solarSystem.getCalculatedAt();
            this.roiToDate = solarSystem.getRoiToDate();
            this.roiAnnualised = solarSystem.getRoiAnnualised();
            this.paybackPeriod = solarSystem.getPaybackPeriod();
            this.breakEvenDate = solarSystem.getBreakEvenDate();
            if (solarSystem.getSolarSystemEnergyPlans() != null) {
                this.energyPlans = solarSystem.getSolarSystemEnergyPlans().stream().map(SolarSystemEnergyPlanDto::new).collect(Collectors.toList());
            }
            if (solarSystem.getSolarSystemIntegrations() != null) {
                this.solarSystemIntegrations = new ArrayList<>();
                for (SolarSystemIntegration solarSystemIntegration : solarSystem.getSolarSystemIntegrations()) {
                    this.solarSystemIntegrations.add(new SolarSystemIntegrationDto(solarSystemIntegration, latestLoadedDataForIntegrations.get(solarSystemIntegration.getId())));
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getOutlayCost() {
        return outlayCost;
    }

    public void setOutlayCost(BigDecimal outlayCost) {
        this.outlayCost = outlayCost;
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

    public ZonedDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(ZonedDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getRoiToDate() {
        return roiToDate;
    }

    public void setRoiToDate(BigDecimal roiToDate) {
        this.roiToDate = roiToDate;
    }

    public BigDecimal getRoiAnnualised() {
        return roiAnnualised;
    }

    public void setRoiAnnualised(BigDecimal roiAnnualised) {
        this.roiAnnualised = roiAnnualised;
    }

    public BigDecimal getPaybackPeriod() {
        return paybackPeriod;
    }

    public void setPaybackPeriod(BigDecimal paybackPeriod) {
        this.paybackPeriod = paybackPeriod;
    }

    public ZonedDateTime getBreakEvenDate() {
        return breakEvenDate;
    }

    public void setBreakEvenDate(ZonedDateTime breakEvenDate) {
        this.breakEvenDate = breakEvenDate;
    }

    public List<SolarSystemEnergyPlanDto> getEnergyPlans() {
        return energyPlans;
    }

    public void setEnergyPlans(List<SolarSystemEnergyPlanDto> energyPlans) {
        this.energyPlans = energyPlans;
    }

    public List<SolarSystemIntegrationDto> getSolarSystemIntegrations() {
        return solarSystemIntegrations;
    }

    public void setSolarSystemIntegrations(List<SolarSystemIntegrationDto> solarSystemIntegrations) {
        this.solarSystemIntegrations = solarSystemIntegrations;
    }

}
