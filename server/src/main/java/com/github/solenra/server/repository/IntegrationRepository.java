package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.Integration;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

	boolean existsByCode(String code);

    Integration findByCode(String codeSolaredge);

}
