package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ENERGY_PLAN_RATE")
public class EnergyPlanRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "RATE_NAME")
    private String rateName;

    @Column(name = "RATE_VALUE", scale = 15, precision = 30)
    private BigDecimal rateValue;

    @Column(name = "COMPARATIVE_RATE_VALUE", scale = 15, precision = 30)
    private BigDecimal comparativeRateValue;

    @OneToMany(mappedBy = "energyPlanRate", cascade = CascadeType.ALL)
    private List<EnergyPlanRatePeriod> energyPlanRatePeriods;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_ID", referencedColumnName = "ID", nullable = false)
    private EnergyPlan energyPlan;

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

    public String getRateName() {
        return rateName;
    }

    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    public BigDecimal getRateValue() {
        return rateValue;
    }

    public void setRateValue(BigDecimal rateValue) {
        this.rateValue = rateValue;
    }

    public BigDecimal getComparativeRateValue() {
        return comparativeRateValue;
    }

    public void setComparativeRateValue(BigDecimal comparativeRateValue) {
        this.comparativeRateValue = comparativeRateValue;
    }

    public List<EnergyPlanRatePeriod> getEnergyPlanRatePeriods() {
        return energyPlanRatePeriods;
    }

    public void setEnergyPlanRatePeriods(List<EnergyPlanRatePeriod> energyPlanRatePeriods) {
        this.energyPlanRatePeriods = energyPlanRatePeriods;
    }

    public EnergyPlan getEnergyPlan() {
        return energyPlan;
    }

    public void setEnergyPlan(EnergyPlan energyPlan) {
        this.energyPlan = energyPlan;
    }

}
