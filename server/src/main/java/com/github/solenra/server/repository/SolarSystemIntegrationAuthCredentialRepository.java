package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.SolarSystemIntegrationAuthCredential;

import java.util.List;

public interface SolarSystemIntegrationAuthCredentialRepository extends JpaRepository<SolarSystemIntegrationAuthCredential, Long> {

    SolarSystemIntegrationAuthCredential findBySolarSystemIntegrationIdAndType(Long solarSystemIntegrationId, String type);

    List<SolarSystemIntegrationAuthCredential> findAllBySolarSystemIntegrationIdAndTypeIn(long solarSystemIntegrationId, List<String> types);

    void deleteAllBySolarSystemIntegrationIdAndTypeIn(Long solarSystemIntegrationId, List<String> typeCodes);

    void deleteAllBySolarSystemIntegrationId(Long solarSystemIntegrationId);

}
