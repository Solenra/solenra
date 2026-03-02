package com.github.solenra.server.model;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.github.solenra.server.entity.EnergyPlanRatePeriod;

public class EnergyPlanRatePeriodDto {

    private Long id;
    private List<EnergyPlanRatePeriodDayDto> daysOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public EnergyPlanRatePeriodDto() {
    }

    public EnergyPlanRatePeriodDto(EnergyPlanRatePeriod energyPlanRatePeriod) {
        this.id = energyPlanRatePeriod.getId();
        if (energyPlanRatePeriod.getDaysOfWeek() != null) {
            this.daysOfWeek = energyPlanRatePeriod.getDaysOfWeek().stream().map(EnergyPlanRatePeriodDayDto::new).collect(Collectors.toList());
        }
        this.startTime = energyPlanRatePeriod.getStartTime();
        this.endTime = energyPlanRatePeriod.getEndTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<EnergyPlanRatePeriodDayDto> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<EnergyPlanRatePeriodDayDto> daysOfWeek) {
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

}
