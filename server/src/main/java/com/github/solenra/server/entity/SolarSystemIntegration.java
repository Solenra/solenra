package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.solenra.server.entity.integration.SystemDetails;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;

@Entity
@Table(name = "SOLAR_SYSTEM_INTEGRATION", uniqueConstraints = {@UniqueConstraint(columnNames = {"SOLAR_SYSTEM_ID", "INTEGRATION_ID"})})
public class SolarSystemIntegration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Boolean enabled = false;

    @Column(name = "AUTH_EXPIRED")
    private Boolean authExpired;

    @OneToMany(mappedBy = "solarSystemIntegration")
    private List<SolarSystemIntegrationAuthCredential> solarSystemIntegrationAuthCredentials;

    @ManyToOne
    @JoinColumn(name = "SOLAR_SYSTEM_INTEGRATION_STATUS_ID", nullable = false)
    private SolarSystemIntegrationStatus status;

    @ManyToOne
    @JoinColumn(name = "SOLAR_SYSTEM_ID", nullable = false)
    private SolarSystem solarSystem;

    @ManyToOne
    @JoinColumn(name = "INTEGRATION_ID", nullable = false)
    private Integration integration;

    @OneToOne(mappedBy = "solarSystemIntegration")
    private SystemDetails systemDetails;

    private String timezone;

    @OneToMany(mappedBy = "solarSystemIntegration")
    private List<SystemEnergyDetails> systemEnergyDetails;

    @Column(name = "NEXT_UPDATE_TIME")
    private ZonedDateTime nextUpdateTime = ZonedDateTime.now();

    @Column(name = "PROCESSING_STARTED_AT")
    private ZonedDateTime processingStartedAt;

    @Column(name = "PROCESSING_HEARTBEAT_AT")
    private ZonedDateTime processingHeartbeatAt;

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

    public List<SolarSystemIntegrationAuthCredential> getSolarSystemIntegrationAuthCredentials() {
        return solarSystemIntegrationAuthCredentials;
    }

    public void setSolarSystemIntegrationAuthCredentials(List<SolarSystemIntegrationAuthCredential> solarSystemIntegrationAuthCredentials) {
        this.solarSystemIntegrationAuthCredentials = solarSystemIntegrationAuthCredentials;
    }

    public SolarSystemIntegrationStatus getStatus() {
        return status;
    }

    public void setStatus(SolarSystemIntegrationStatus status) {
        this.status = status;
    }

    public SolarSystem getSolarSystem() {
        return solarSystem;
    }

    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public SystemDetails getSystemDetails() {
        return systemDetails;
    }

    public void setSystemDetails(SystemDetails systemDetails) {
        this.systemDetails = systemDetails;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<SystemEnergyDetails> getSystemEnergyDetails() {
        return systemEnergyDetails;
    }

    public void setSystemEnergyDetails(List<SystemEnergyDetails> systemEnergyDetails) {
        this.systemEnergyDetails = systemEnergyDetails;
    }

    public ZonedDateTime getNextUpdateTime() {
        return nextUpdateTime;
    }

    public void setNextUpdateTime(ZonedDateTime nextUpdateTime) {
        this.nextUpdateTime = nextUpdateTime;
    }

    public ZonedDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(ZonedDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public ZonedDateTime getProcessingHeartbeatAt() {
        return processingHeartbeatAt;
    }

    public void setProcessingHeartbeatAt(ZonedDateTime processingHeartbeatAt) {
        this.processingHeartbeatAt = processingHeartbeatAt;
    }

}
