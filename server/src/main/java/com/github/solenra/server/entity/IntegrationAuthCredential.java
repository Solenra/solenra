package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "INTEGRATION_AUTH_CREDENTIAL", uniqueConstraints = {@UniqueConstraint(columnNames = {"INTEGRATION_ID", "TYPE"})})
public class IntegrationAuthCredential {

    public static final String TYPE_ACCOUNT_KEY = "account-key";
    public static final String TYPE_CLIENT_ID = "client-id";
    public static final String TYPE_CLIENT_SECRET = "client-secret";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "INTEGRATION_ID", nullable = false)
    private Integration integration;

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

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
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
