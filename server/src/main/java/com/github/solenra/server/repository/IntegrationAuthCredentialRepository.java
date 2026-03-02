package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.IntegrationAuthCredential;

public interface IntegrationAuthCredentialRepository extends JpaRepository<IntegrationAuthCredential, Long> {

    IntegrationAuthCredential findByIntegrationCodeAndType(String integrationCode, String type);

}
