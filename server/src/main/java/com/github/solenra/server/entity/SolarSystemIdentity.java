package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "SOLAR_SYSTEM_IDENTITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"SOLAR_SYSTEM_ID", "IDENTITY_ID"})})
public class SolarSystemIdentity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "SOLAR_SYSTEM_ID", nullable = false)
    private SolarSystem solarSystem;

    @ManyToOne
    @JoinColumn(name = "IDENTITY_ID", nullable = false)
    private Identity identity;

}
