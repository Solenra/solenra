package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SOLAR_SYSTEM_INTEGRATION_AUTH_CREDENTIAL", uniqueConstraints = {@UniqueConstraint(columnNames = {"SOLAR_SYSTEM_INTEGRATION_ID", "TYPE"})})
public class SolarSystemIntegrationAuthCredential {

    public static final String TYPE_SYSTEM_ID = "system-id";
    public static final String TYPE_API_KEY = "api-key";
    public static final String TYPE_ACCESS_TOKEN = "access-token";
    public static final String TYPE_REFRESH_TOKEN = "refresh-token";
    public static final String TYPE_AUTH_CODE = "auth-code";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "SOLAR_SYSTEM_INTEGRATION_ID", nullable = false)
    private SolarSystemIntegration solarSystemIntegration;

    private String type;

    @Column(name = "CONTENT")
    private String value;

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

    public SolarSystemIntegration getSolarSystemIntegration() {
        return solarSystemIntegration;
    }

    public void setSolarSystemIntegration(SolarSystemIntegration solarSystemIntegration) {
        this.solarSystemIntegration = solarSystemIntegration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
