package com.github.solenra.server.entity.integration;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.model.solaredge.Details;
import com.github.solenra.server.model.solaredgev2.SiteDetails;

@Entity
@Table(name = "SYSTEM_DETAILS")
public class SystemDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @OneToOne//(cascade = CascadeType.ALL)
    @JoinColumn(name = "SOLAR_SYSTEM_INTEGRATION_ID", referencedColumnName = "ID")
    private SolarSystemIntegration solarSystemIntegration;

    private String status;

    @Column(name = "PEAK_POWER")
    private String peakPower;

    @Column(name = "LAST_UPDATE_TIME")
    private ZonedDateTime lastUpdateTime;

    @Column(name = "INSTALLATION_DATE")
    private ZonedDateTime installationDate;

    private String timezone;

    private String notes;

    private String type;

    //private SystemLocation location;

    //@Column(name = "PRIMARY_MODULE")
    //private SystemModule primaryModule;

    public static SystemDetails convertToEntity(Details.Root detailsRoot) {
        SystemDetails systemDetails = new SystemDetails();
        systemDetails.setStatus(detailsRoot.details.status);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        systemDetails.setInstallationDate(
                ZonedDateTime.of(
                        LocalDate.parse(detailsRoot.details.installationDate, dateTimeFormatter),
                        LocalTime.MIDNIGHT,
                        ZoneId.of(detailsRoot.details.location.timeZone)
                )
        );
        systemDetails.setTimezone(detailsRoot.details.location.timeZone);
        systemDetails.setPeakPower(String.valueOf(detailsRoot.details.peakPower));

        return systemDetails;
    }

    public static SystemDetails convertToEntity(SiteDetails details, SolarSystemIntegration solarSystemIntegration) {
        SystemDetails systemDetails = new SystemDetails();
        systemDetails.setStatus(details.getActivationStatus());
        systemDetails.setPeakPower(String.valueOf(details.getPeakPower()));
        systemDetails.setTimezone(solarSystemIntegration.getTimezone());
        ZonedDateTime installationDate = null;
        if (solarSystemIntegration.getTimezone() != null) {
            installationDate = ZonedDateTime.of(
                    details.installationDate.toLocalDate(),
                    LocalTime.MIDNIGHT,
                    ZoneId.of(solarSystemIntegration.getTimezone())
            );
        } else {
            installationDate = ZonedDateTime.of(
                    details.installationDate.toLocalDate(),
                    LocalTime.MIDNIGHT,
                    ZoneId.systemDefault()
            );
        }
        systemDetails.setInstallationDate(installationDate);
        return systemDetails;
    }

    public SolarSystemIntegration getSolarSystemIntegration() {
        return solarSystemIntegration;
    }

    public void setSolarSystemIntegration(SolarSystemIntegration solarSystemIntegration) {
        this.solarSystemIntegration = solarSystemIntegration;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPeakPower() {
        return peakPower;
    }

    public void setPeakPower(String peakPower) {
        this.peakPower = peakPower;
    }

    public ZonedDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(ZonedDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public ZonedDateTime getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(ZonedDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
