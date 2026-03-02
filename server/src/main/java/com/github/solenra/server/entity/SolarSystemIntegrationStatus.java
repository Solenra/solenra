package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SOLAR_SYSTEM_INTEGRATION_STATUS", uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE"})})
public class SolarSystemIntegrationStatus {

    public static final String CODE_PENDING = "pending";
    public static final String CODE_LOADING_FROM_INTEGRATION_QUEUED = "loading-from-integration-queued";
    public static final String CODE_LOADING_FROM_INTEGRATION_PROCESSING = "loading-from-integration-processing";
    public static final String CODE_LOADING_FROM_INTEGRATION_TRANSIENT_ERROR = "loading-from-integration-transient-error";
    public static final String CODE_LOADING_FROM_INTEGRATION_ERROR = "loading-from-integration-error";
    public static final String CODE_UP_TO_DATE = "up-to-date";
    public static final String CODE_EXPIRED = "expired";
    public static final String CODE_EXPIRED_CREDENTIALS = "expired-credentials";
    public static final String CODE_SETUP = "setup";
    public static final String CODE_DISABLED = "disabled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    private String description;

    @Column(name = "DISPLAY_ORDER")
    private Long displayOrder;

    @Column(name = "AUTO_RELOAD")
    private Boolean autoReload;

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
