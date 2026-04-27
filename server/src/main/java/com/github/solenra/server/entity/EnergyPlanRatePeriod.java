package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "ENERGY_PLAN_RATE_PERIOD")
public class EnergyPlanRatePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @OneToMany(mappedBy = "energyPlanRatePeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnergyPlanRatePeriodDay> daysOfWeek;

    @Column(name = "START_TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME")
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "ENERGY_PLAN_RATE_ID", referencedColumnName = "ID", nullable = false)
    private EnergyPlanRate energyPlanRate;

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

    public List<EnergyPlanRatePeriodDay> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<EnergyPlanRatePeriodDay> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public EnergyPlanRate getEnergyPlanRate() {
        return energyPlanRate;
    }

    public void setEnergyPlanRate(EnergyPlanRate energyPlanRate) {
        this.energyPlanRate = energyPlanRate;
    }

}
