package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

import com.github.solenra.server.entity.integration.SystemEnergyDetails;

@Entity
@Table(name = "SYSTEM_ENERGY_DETAILS_REVENUE")
public class SystemEnergyDetailsRevenue {

    public static final String CALCULATION_STATUS_PENDING = "PENDING";
    public static final String CALCULATION_STATUS_CALCULATED = "CALCULATED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "CALCULATION_STATUS")
    private String calculationStatus = CALCULATION_STATUS_PENDING;

    @ManyToOne
    @JoinColumn(name = "SYSTEM_ENERGY_DETAILS_ID", nullable = false)
    private SystemEnergyDetails systemEnergyDetails;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_RATE_PERIOD_ID", nullable = false)
    private EnergyPlanRatePeriod energyPlanRatePeriod;

    @Column(name = "SUPPLY_COST", scale = 15, precision = 30)
    private BigDecimal supplyCost;

    @Column(name = "IMPORT_COST", scale = 15, precision = 30)
    private BigDecimal importCost;

    @Column(name = "EXPORT_REVENUE", scale = 15, precision = 30)
    private BigDecimal exportRevenue;

    @Column(name = "SELF_CONSUMPTION_SAVINGS", scale = 15, precision = 30)
    private BigDecimal selfConsumptionSavings;

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

    public String getCalculationStatus() {
        return calculationStatus;
    }

    public void setCalculationStatus(String calculationStatus) {
        this.calculationStatus = calculationStatus;
    }

    public SystemEnergyDetails getSystemEnergyDetails() {
        return systemEnergyDetails;
    }

    public void setSystemEnergyDetails(SystemEnergyDetails systemEnergyDetails) {
        this.systemEnergyDetails = systemEnergyDetails;
    }

    public EnergyPlanRatePeriod getEnergyPlanRatePeriod() {
        return energyPlanRatePeriod;
    }

    public void setEnergyPlanRatePeriod(EnergyPlanRatePeriod energyPlanRatePeriod) {
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
