package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.solenra.server.model.SolarSystemDto;

@Entity
@Table(name = "SOLAR_SYSTEM")
public class SolarSystem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String name;

    private String timezone;

    private String notes;

    @OneToMany(mappedBy = "solarSystem")
    private List<SolarSystemIntegration> solarSystemIntegrations;

    @OneToMany(mappedBy = "solarSystem")
    private List<SolarSystemEnergyPlan> solarSystemEnergyPlans;

    @Column(name = "OUTLAY_COST", scale = 15, precision = 30)
    private BigDecimal outlayCost;

    @Column(name = "CUMULATIVE_SUPPLY_COST", scale = 15, precision = 30)
    private BigDecimal cumulativeSupplyCost;

    @Column(name = "CUMULATIVE_IMPORT_COST", scale = 15, precision = 30)
    private BigDecimal cumulativeImportCost;

    @Column(name = "CUMULATIVE_EXPORT_REVENUE", scale = 15, precision = 30)
    private BigDecimal cumulativeExportRevenue;

    @Column(name = "CUMULATIVE_SELF_CONSUMPTION_SAVINGS", scale = 15, precision = 30)
    private BigDecimal cumulativeSelfConsumptionSavings;

    @Column(name = "CALCULATED_SAVINGS", scale = 15, precision = 30)
    private BigDecimal calculatedSavings;

    @Column(name = "CALCULATED_AT")
    private ZonedDateTime calculatedAt;

    @Column(name = "ROI_TO_DATE", scale = 15, precision = 30)
    private BigDecimal roiToDate;

    @Column(name = "ROI_ANNUALISED", scale = 15, precision = 30)
    private BigDecimal roiAnnualised;

    @Column(name = "PAYBACK_PERIOD", scale = 15, precision = 30)
    private BigDecimal paybackPeriod;

    @Column(name = "BREAK_EVEN_DATE")
    private ZonedDateTime breakEvenDate;

    public static SolarSystem convertToEntity(SolarSystemDto solarSystemDto, SolarSystem solarSystem) {
        if (solarSystem == null) {
            solarSystem = new SolarSystem();
        }
        solarSystem.setName(solarSystemDto.getName());
        solarSystem.setTimezone(solarSystemDto.getTimezone());
        solarSystem.setNotes(solarSystemDto.getNotes());
        solarSystem.setOutlayCost(solarSystemDto.getOutlayCost());
        return solarSystem;
    }

    public static SolarSystem convertToEntity(SolarSystemDto solarSystemDto) {
        return convertToEntity(solarSystemDto, null);
    }

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

    public List<SolarSystemIntegration> getSolarSystemIntegrations() {
        return solarSystemIntegrations;
    }

    public void setSolarSystemIntegrations(List<SolarSystemIntegration> solarSystemIntegrations) {
        this.solarSystemIntegrations = solarSystemIntegrations;
    }

    public List<SolarSystemEnergyPlan> getSolarSystemEnergyPlans() {
        return solarSystemEnergyPlans;
    }

    public void setSolarSystemEnergyPlans(List<SolarSystemEnergyPlan> solarSystemEnergyPlans) {
        this.solarSystemEnergyPlans = solarSystemEnergyPlans;
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

}
