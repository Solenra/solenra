package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.time.DayOfWeek;

@Entity
@Table(name = "ENERGY_PLAN_RATE_PERIOD_DAY")
public class EnergyPlanRatePeriodDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private DayOfWeek dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_RATE_PERIOD_ID", referencedColumnName = "ID", nullable = false)
    private EnergyPlanRatePeriod energyPlanRatePeriod;

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

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public EnergyPlanRatePeriod getEnergyPlanRatePeriod() {
        return energyPlanRatePeriod;
    }

    public void setEnergyPlanRatePeriod(EnergyPlanRatePeriod energyPlanRatePeriod) {
        this.energyPlanRatePeriod = energyPlanRatePeriod;
    }

}
