package com.github.solenra.server.service;

import java.util.List;

import com.github.solenra.server.model.ConfigDto;

public interface ConfigService {

    List<ConfigDto> getConfigList();

    String getConfigValue(String code);

    ConfigDto getConfig(String code);

    void saveConfig(String code, String value);

}
