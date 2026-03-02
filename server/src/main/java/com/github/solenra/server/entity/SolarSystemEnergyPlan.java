package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "SOLAR_SYSTEM_ENERGY_PLAN")
public class SolarSystemEnergyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "SOLAR_SYSTEM_ID", nullable = false)
    private SolarSystem solarSystem;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_ID", nullable = false)
    private EnergyPlan energyPlan;

    @Column(name = "START_DATE", nullable = false)
    private ZonedDateTime startDate;

    @Column(name = "END_DATE")
    private ZonedDateTime endDate;

    @Column(name = "INCLUDE_IN_REVENUE_CALCULATION")
    private Boolean includeInRevenueCalculation = true;

    @Column(name = "CUMULATIVE_SUPPLY_COST", scale = 15, precision = 30)
    private BigDecimal cumulativeSupplyCost = BigDecimal.ZERO;

    @Column(name = "CUMULATIVE_IMPORT_COST", scale = 15, precision = 30)
    private BigDecimal cumulativeImportCost = BigDecimal.ZERO;

    @Column(name = "CUMULATIVE_EXPORT_REVENUE", scale = 15, precision = 30)
    private BigDecimal cumulativeExportRevenue = BigDecimal.ZERO;

    @Column(name = "CUMULATIVE_SELF_CONSUMPTION_SAVINGS", scale = 15, precision = 30)
    private BigDecimal cumulativeSelfConsumptionSavings = BigDecimal.ZERO;

    @Column(name = "CALCULATED_SAVINGS", scale = 15, precision = 30)
    private BigDecimal calculatedSavings = BigDecimal.ZERO;

    @Column(name = "ROI_TO_DATE", scale = 15, precision = 30)
    private BigDecimal roiToDate;

    @Column(name = "ROI_ANNUALISED", scale = 15, precision = 30)
    private BigDecimal roiAnnualised;

    @Column(name = "CALCULATED_AT")
    private ZonedDateTime calculatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public SolarSystem getSolarSystem() {
        return solarSystem;
    }

    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public EnergyPlan getEnergyPlan() {
        return energyPlan;
    }

    public void setEnergyPlan(EnergyPlan energyPlan) {
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

    public ZonedDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(ZonedDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

}
