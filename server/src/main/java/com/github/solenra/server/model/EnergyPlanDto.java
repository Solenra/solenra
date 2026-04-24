package com.github.solenra.server.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.github.solenra.server.entity.EnergyPlan;

public class EnergyPlanDto {

    private Long id;
    private String name;
    private String notes;
    private Boolean shared;
    private BigDecimal supplyRateValue;
    private BigDecimal exportRateValue;
    private List<EnergyPlanRateDto> energyPlanRates;
    private StatusDto status;

    public EnergyPlanDto() {
    }

    public EnergyPlanDto(EnergyPlan energyPlan) {
        this.id = energyPlan.getId();
        this.name = energyPlan.getName();
        this.notes = energyPlan.getNotes();
        this.shared = energyPlan.getShared();
        this.supplyRateValue = energyPlan.getSupplyRateValue();
        this.exportRateValue = energyPlan.getExportRateValue();
        if (energyPlan.getEnergyPlanRates() != null) {
            this.energyPlanRates = energyPlan.getEnergyPlanRates().stream().map(EnergyPlanRateDto::new).collect(Collectors.toList());
        }
        if (energyPlan.getStatus() != null) {
            this.status = new StatusDto(energyPlan.getStatus());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<EnergyPlanRateDto> getEnergyPlanRates() {
        return energyPlanRates;
    }

    public void setEnergyPlanRates(List<EnergyPlanRateDto> energyPlanRates) {
        this.energyPlanRates = energyPlanRates;
    }

    public StatusDto getStatus() {
        return status;
    }

    public void setStatus(StatusDto status) {
        this.status = status;
    }

}
