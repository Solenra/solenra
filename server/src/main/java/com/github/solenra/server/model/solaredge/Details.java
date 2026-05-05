package com.github.solenra.server.model.solaredge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Details {

    public Integer id;
    public String name;
    public Integer accountId;
    public String status;
    public Double peakPower;
    public String lastUpdateTime;
    public String currency;
    public String installationDate;
    public Object ptoDate;
    public String notes;
    public String type;
    public Location location;
    public PrimaryModule primaryModule;
    public Uris uris;
    public PublicSettings publicSettings;

    @Override
    public String toString() {
        return "Details{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accountId=" + accountId +
                ", status='" + status + '\'' +
                ", peakPower=" + peakPower +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", currency='" + currency + '\'' +
                ", installationDate='" + installationDate + '\'' +
                ", ptoDate=" + ptoDate +
                ", notes='" + notes + '\'' +
                ", type='" + type + '\'' +
                ", location=" + location +
                ", primaryModule=" + primaryModule +
                ", uris=" + uris +
                ", publicSettings=" + publicSettings +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        public String country;
        public String state;
        public String city;
        public String address;
        public String address2;
        public String zip;
        public String timeZone;
        public String countryCode;
        public String stateCode;

        @Override
        public String toString() {
            return "Location{" +
                    "country='" + country + '\'' +
                    ", state='" + state + '\'' +
                    ", city='" + city + '\'' +
                    ", address='" + address + '\'' +
                    ", address2='" + address2 + '\'' +
                    ", zip='" + zip + '\'' +
                    ", timeZone='" + timeZone + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    ", stateCode='" + stateCode + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrimaryModule {
        public String manufacturerName;
        public String modelName;
        public int maximumPower;
        public double temperatureCoef;

        @Override
        public String toString() {
            return "PrimaryModule{" +
                    "manufacturerName='" + manufacturerName + '\'' +
                    ", modelName='" + modelName + '\'' +
                    ", maximumPower=" + maximumPower +
                    ", temperatureCoef=" + temperatureCoef +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicSettings {
        public String name;
        public Boolean isPublic;

        @Override
        public String toString() {
            return "PublicSettings{" +
                    "name='" + name + '\'' +
                    ", isPublic=" + isPublic +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Root {
        public Details details;

        @Override
        public String toString() {
            return "Root{" +
                    "details=" + details +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Uris {
        @JsonProperty("PUBLIC_URL")
        public String publicUrl;
        @JsonProperty("SITE_IMAGE")
        public String siteImage;
        @JsonProperty("DATA_PERIOD")
        public String dataPeriod;
        @JsonProperty("DETAILS")
        public String details;
        @JsonProperty("OVERVIEW")
        public String overview;

        @Override
        public String toString() {
            return "Uris{" +
                    "publicUrl='" + publicUrl + '\'' +
                    ", siteImage='" + siteImage + '\'' +
                    ", dataPeriod='" + dataPeriod + '\'' +
                    ", details='" + details + '\'' +
                    ", overview='" + overview + '\'' +
                    '}';
        }
    }

}

