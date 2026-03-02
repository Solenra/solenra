package com.github.solenra.server.service;

import java.util.List;
import java.util.Map;

public interface IntegrationAuthService {

    String getCredential(String integrationCode, String type);

    void saveCredential(Long solarSystemIntegrationId, String typeCode, String value);

    void deleteCredentials(Long solarSystemIntegrationId, List<String> typeCodes);

    Map<String, String> getCredentials(long solarSystemIntegrationId, List<String> types);

    String getCredential(long solarSystemIntegrationId, String type);

    String getAccessToken(long solarSystemIntegrationId, boolean forceRefresh);

}
