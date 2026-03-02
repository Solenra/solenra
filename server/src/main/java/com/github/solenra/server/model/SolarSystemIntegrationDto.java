package com.github.solenra.server.model;

import java.time.ZonedDateTime;

import com.github.solenra.server.entity.SolarSystemIntegration;

public class SolarSystemIntegrationDto {

    private Long id;
    private StatusDto status;
    private Boolean enabled;
    private Boolean authExpired;
    private IntegrationDto integration;
    private SystemDetailsDto systemDetails;
    private ZonedDateTime latestLoadedDate;

    public SolarSystemIntegrationDto() {
    }

    public SolarSystemIntegrationDto(SolarSystemIntegration solarSystemIntegration) {
        this(solarSystemIntegration, null);
    }

    public SolarSystemIntegrationDto(SolarSystemIntegration solarSystemIntegration, ZonedDateTime latestLoadedDate) {
        if (solarSystemIntegration != null) {
            this.id = solarSystemIntegration.getId();
            this.status = new StatusDto(solarSystemIntegration.getStatus());
            this.enabled = solarSystemIntegration.getEnabled();
            this.authExpired = solarSystemIntegration.getAuthExpired();
            this.integration = new IntegrationDto(solarSystemIntegration.getIntegration());
            this.systemDetails = new SystemDetailsDto(solarSystemIntegration.getSystemDetails());
        }
        this.latestLoadedDate = latestLoadedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusDto getStatus() {
        return status;
    }

    public void setStatus(StatusDto status) {
        this.status = status;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAuthExpired() {
        return authExpired;
    }

    public void setAuthExpired(Boolean authExpired) {
        this.authExpired = authExpired;
    }

    public IntegrationDto getIntegration() {
        return integration;
    }

    public void setIntegration(IntegrationDto integration) {
        this.integration = integration;
    }

    public SystemDetailsDto getSystemDetails() {
        return systemDetails;
    }

    public void setSystemDetails(SystemDetailsDto systemDetails) {
        this.systemDetails = systemDetails;
    }

    public ZonedDateTime getLatestLoadedDate() {
        return latestLoadedDate;
    }

    public void setLatestLoadedDate(ZonedDateTime latestLoadedDate) {
        this.latestLoadedDate = latestLoadedDate;
    }

}
