package com.github.solenra.server.model;

import com.github.solenra.server.entity.SolarSystemIntegrationStatus;
import com.github.solenra.server.entity.EnergyPlanStatus;

public class StatusDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long displayOrder;
    private Boolean autoReload;

    public StatusDto() {
    }

    public StatusDto(SolarSystemIntegrationStatus status) {
        this.id = status.getId();
        this.code = status.getCode();
        this.name = status.getName();
        this.description = status.getDescription();
        this.displayOrder = status.getDisplayOrder();
        this.autoReload = status.getAutoReload();
    }

    public StatusDto(EnergyPlanStatus status) {
        this.id = status.getId();
        this.code = status.getCode();
        this.name = status.getName();
        this.description = status.getDescription();
        this.displayOrder = status.getDisplayOrder();
        this.autoReload = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getAutoReload() {
        return autoReload;
    }

    public void setAutoReload(Boolean autoReload) {
        this.autoReload = autoReload;
    }

}
