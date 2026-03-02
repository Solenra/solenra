package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ENERGY_PLAN")
public class EnergyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String name;

    private String notes;

    private Boolean shared;

    @OneToMany(mappedBy = "energyPlan")
    private List<SolarSystemEnergyPlan> solarSystemEnergyPlans;

    @Column(name = "SUPPLY_RATE_VALUE", scale = 15, precision = 30)
    private BigDecimal supplyRateValue;

    @Column(name = "EXPORT_RATE_VALUE", scale = 15, precision = 30)
    private BigDecimal exportRateValue;

    @OneToMany(mappedBy = "energyPlan", cascade = CascadeType.ALL)
    private List<EnergyPlanRate> energyPlanRates;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_STATUS_ID", nullable = false)
    private EnergyPlanStatus status;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public List<SolarSystemEnergyPlan> getSolarSystemEnergyPlans() {
        return solarSystemEnergyPlans;
    }

    public void setSolarSystemEnergyPlans(List<SolarSystemEnergyPlan> solarSystemEnergyPlans) {
        this.solarSystemEnergyPlans = solarSystemEnergyPlans;
    }

    public BigDecimal getSupplyRateValue() {
        return supplyRateValue;
    }

    public void setSupplyRateValue(BigDecimal supplyRateValue) {
        this.supplyRateValue = supplyRateValue;
    }

    public BigDecimal getExportRateValue() {
        return exportRateValue;
    }

    public void setExportRateValue(BigDecimal exportRateValue) {
        this.exportRateValue = exportRateValue;
    }

    public List<EnergyPlanRate> getEnergyPlanRates() {
        return energyPlanRates;
    }

    public void setEnergyPlanRates(List<EnergyPlanRate> energyPlanRates) {
        this.energyPlanRates = energyPlanRates;
    }

    public EnergyPlanStatus getStatus() {
        return status;
    }

    public void setStatus(EnergyPlanStatus status) {
        this.status = status;
    }

}
