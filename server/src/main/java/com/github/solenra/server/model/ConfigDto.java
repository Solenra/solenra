package com.github.solenra.server.model;

import com.github.solenra.server.entity.Config;

public class ConfigDto {

    private String code;
    private String value;

    public ConfigDto(Config configEntity) {
        this.code = configEntity.getCode();
        this.value = configEntity.getValue();
    }

    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
