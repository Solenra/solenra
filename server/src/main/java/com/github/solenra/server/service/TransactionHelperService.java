package com.github.solenra.server.service;

import java.time.ZonedDateTime;

public interface TransactionHelperService {

    void saveSolarSystemIntegrationStatus(Long id, String statusCode, ZonedDateTime nextUpdateTime);

    void saveSolarSystemIntegrationProcessingHeartbeat(Long id, ZonedDateTime heartbeatZonedDateTime);

}
