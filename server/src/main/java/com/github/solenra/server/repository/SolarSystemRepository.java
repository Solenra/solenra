package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.SolarSystem;

public interface SolarSystemRepository extends JpaRepository<SolarSystem, Long> {

}
