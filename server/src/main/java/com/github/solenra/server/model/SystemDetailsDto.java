package com.github.solenra.server.model;

import java.time.ZonedDateTime;

import com.github.solenra.server.entity.integration.SystemDetails;

public class SystemDetailsDto {

    private ZonedDateTime installationDate;
    private String timezone;

    public SystemDetailsDto() {
    }

    public SystemDetailsDto(SystemDetails systemDetails) {
        if (systemDetails != null) {
            this.installationDate = systemDetails.getInstallationDate();
            this.timezone = systemDetails.getTimezone();
        }
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

}
