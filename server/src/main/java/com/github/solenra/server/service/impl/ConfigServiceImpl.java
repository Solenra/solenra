package com.github.solenra.server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.solenra.server.entity.Config;
import com.github.solenra.server.model.ConfigDto;
import com.github.solenra.server.repository.ConfigRepository;
import com.github.solenra.server.service.ConfigService;

@Service("configService")
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;

    public ConfigServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public List<ConfigDto> getConfigList() {
        // TODO permission check
        List<Config> configs = configRepository.findAll();
        return configs.stream().map(ConfigDto::new).toList();
    }

    private Config getConfigEntity(String code) {
        Config config = configRepository.findByCode(code);
        if (config == null) {
            throw new RuntimeException("Config with code [" + code + "] not found.");
        }
        return config;
    }

    @Override
    public String getConfigValue(String code) {
        // TODO permission check
        return getConfigEntity(code).getValue();
    }

    @Override
    public ConfigDto getConfig(String code) {
        // TODO permission check
        return new ConfigDto(getConfigEntity(code));
    }

    @Override
    public void saveConfig(String code, String value) {
        // TODO permission check
        Config config = getConfigEntity(code);
        config.setValue(value);
        config = configRepository.save(config);
    }

}
