package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.EnergyPlanStatus;

public interface EnergyPlanStatusRepository extends JpaRepository<EnergyPlanStatus, Long> {

    EnergyPlanStatus findByCode(String codePublished);

}
