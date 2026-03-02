package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "PERMISSION", uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE"})})
public class Permission {

    public static final String CODE_IDENTITY_ROLE_ADMIN_WRITE = "identity-role.admin.write";
    public static final String CODE_IDENTITY_ROLE_READ = "identity-role.read";
    public static final String CODE_IDENTITY_ROLE_WRITE = "identity-role.write";
    public static final String CODE_SOLAR_SYSTEM_ACCESS_ALL = "solar-system.access-all";
    public static final String CODE_SOLAR_SYSTEM_READ = "solar-system.read";
    public static final String CODE_SOLAR_SYSTEM_CREATE = "solar-system.create";
    public static final String CODE_SOLAR_SYSTEM_UPDATE = "solar-system.update";
    public static final String CODE_SOLAR_SYSTEM_DELETE = "solar-system.delete";
    public static final String CODE_SOLAR_SYSTEM_INITIALISE = "solar-system.initialise";

    public static final String CODE_AUDIT_SOLAR_SYSTEM_READ = "audit.solar-system.read";
    public static final String CODE_AUDIT_IDENTITY_ROLE_READ = "audit.identity-role.read";

    public static final String CODE_ADMIN_SCHEDULER = "admin.scheduler";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String code;

    private String name;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
