package com.github.solenra.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.solenra.server.model.ConfigDto;
import com.github.solenra.server.service.ConfigService;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @RequestMapping("/")
    public List<ConfigDto> getConfigList() {
        return configService.getConfigList();
    }

    @RequestMapping("/{configCode}")
    public ConfigDto getConfig(@PathVariable String configCode) {
        return configService.getConfig(configCode);
    }

    @PostMapping("/{configCode}")
    public ResponseEntity<?> saveConfig(@PathVariable String configCode, @RequestBody(required = false) String body) {
        configService.saveConfig(configCode, body);
        return ResponseEntity.ok().build();
    }

}
