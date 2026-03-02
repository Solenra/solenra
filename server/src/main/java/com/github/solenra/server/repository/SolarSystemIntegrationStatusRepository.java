package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.SolarSystemIntegrationStatus;

public interface SolarSystemIntegrationStatusRepository extends JpaRepository<SolarSystemIntegrationStatus, Long> {

    SolarSystemIntegrationStatus findByCode(String code);

}
