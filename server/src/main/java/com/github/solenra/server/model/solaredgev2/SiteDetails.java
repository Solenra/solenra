package com.github.solenra.server.model.solaredgev2;

import java.time.ZonedDateTime;

public class SiteDetails {

    public String activationStatus;
    public ZonedDateTime installationDate;
    public ZonedDateTime lastUpdateTime;
    public Location location;
    public String name;
    public String note;
    public double peakPower;
    public int siteId;

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public ZonedDateTime getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(ZonedDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public ZonedDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(ZonedDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getPeakPower() {
        return peakPower;
    }

    public void setPeakPower(double peakPower) {
        this.peakPower = peakPower;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public static class Location {

        public String address;
        public String city;
        public String country;
        public String state;
        public String zip;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

    }

}
