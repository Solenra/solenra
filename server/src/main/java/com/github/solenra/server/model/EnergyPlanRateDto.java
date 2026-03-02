package com.github.solenra.server.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.github.solenra.server.entity.EnergyPlanRate;

public class EnergyPlanRateDto {

    private Long id;
    private String rateName;
    private BigDecimal rateValue;
    private BigDecimal comparativeRateValue;
    private List<EnergyPlanRatePeriodDto> energyPlanRatePeriods;

    public EnergyPlanRateDto() {
    }

    public EnergyPlanRateDto(EnergyPlanRate energyPlanRate) {
        this.id = energyPlanRate.getId();
        this.rateName = energyPlanRate.getRateName();
        this.rateValue = energyPlanRate.getRateValue();
        this.comparativeRateValue = energyPlanRate.getComparativeRateValue();
        if (energyPlanRate.getEnergyPlanRatePeriods() != null) {
            this.energyPlanRatePeriods = energyPlanRate.getEnergyPlanRatePeriods().stream().map(EnergyPlanRatePeriodDto::new).collect(Collectors.toList());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<EnergyPlanRatePeriodDto> getEnergyPlanRatePeriods() {
        return energyPlanRatePeriods;
    }

    public void setEnergyPlanRatePeriods(List<EnergyPlanRatePeriodDto> energyPlanRatePeriods) {
        this.energyPlanRatePeriods = energyPlanRatePeriods;
    }

}
