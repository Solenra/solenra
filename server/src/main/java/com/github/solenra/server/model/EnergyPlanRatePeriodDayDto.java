package com.github.solenra.server.model;

import java.time.DayOfWeek;

import com.github.solenra.server.entity.EnergyPlanRatePeriodDay;

public class EnergyPlanRatePeriodDayDto {

    private Long id;
    private DayOfWeek dayOfWeek;

    public EnergyPlanRatePeriodDayDto() {
    }

    public EnergyPlanRatePeriodDayDto(EnergyPlanRatePeriodDay energyPlanRatePeriodDay) {
        this.id = energyPlanRatePeriodDay.getId();
        this.dayOfWeek = energyPlanRatePeriodDay.getDayOfWeek();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

}
