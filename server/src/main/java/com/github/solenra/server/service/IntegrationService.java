package com.github.solenra.server.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.github.solenra.server.model.IntegrationDto;

public interface IntegrationService {

    List<IntegrationDto> getIntegrations(Principal principal);

    void saveIntegration(Principal principal, Map<String, Object> integrationData);

}
