package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ENERGY_PLAN_STATUS", uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE"})})
public class EnergyPlanStatus {

    public static final String CODE_DRAFT = "draft";
    public static final String CODE_PUBLISHED = "published";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String code;

    private String name;

    private String description;

    @Column(name = "DISPLAY_ORDER")
    private Long displayOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

}
