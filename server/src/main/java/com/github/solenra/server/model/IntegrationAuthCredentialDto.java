package com.github.solenra.server.model;

import com.github.solenra.server.entity.IntegrationAuthCredential;

public class IntegrationAuthCredentialDto {

    private String type;
    private String value;

    public IntegrationAuthCredentialDto(IntegrationAuthCredential integrationAuthCredential) {
        this.type = integrationAuthCredential.getType();
        this.value = integrationAuthCredential.getValue();
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
