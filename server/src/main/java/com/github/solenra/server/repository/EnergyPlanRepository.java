package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.EnergyPlan;

public interface EnergyPlanRepository extends JpaRepository<EnergyPlan, Long> {

}
