package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.Config;

public interface ConfigRepository extends JpaRepository<Config, Long> {

    boolean existsByCode(String configCode);

    Config findByCode(String code);

}
