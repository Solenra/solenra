package com.github.solenra.server.model;

import java.util.ArrayList;
import java.util.List;

import com.github.solenra.server.entity.Integration;
import com.github.solenra.server.entity.IntegrationAuthCredential;

public class IntegrationDto {

    private Long id;
    private Boolean enabled;
    private String code;
    private String name;
    private List<IntegrationAuthCredentialDto> credentials;

    public IntegrationDto() {
    }

    public IntegrationDto(Integration integration) {
        if (integration != null) {
            this.id = integration.getId();
            this.enabled = integration.getEnabled();
            this.code = integration.getCode();
            this.name = integration.getName();

            if (integration.getIntegrationAuthCredentials() != null) {
                credentials = new ArrayList<>();
                for (IntegrationAuthCredential integrationAuthCredential : integration.getIntegrationAuthCredentials()) {
                    credentials.add(new IntegrationAuthCredentialDto(integrationAuthCredential));
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public List<IntegrationAuthCredentialDto> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<IntegrationAuthCredentialDto> credentials) {
        this.credentials = credentials;
    }

}
