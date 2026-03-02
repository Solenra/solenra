package com.github.solenra.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.solenra.server.entity.Identity;

public interface IdentityRepository extends JpaRepository<Identity, Long> {

	boolean existsByUsername(String adminUsername);

    Identity findByUsername(String username);

}
