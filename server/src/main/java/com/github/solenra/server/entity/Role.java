package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ROLE", uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE"})})
public class Role {

    public static final String CODE_ADMIN = "admin";
    public static final String CODE_SUPPORT = "support";
    public static final String CODE_USER = "user";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String code;

    private String name;

    private Long hierarchy;

    @ManyToMany
    @JoinTable(
            name = "ROLE_PERMISSION",
            joinColumns = @JoinColumn(name = "ROLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID")
    )
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<IdentityRole> identityRoles;

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

    public Long getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(Long hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<IdentityRole> getIdentityRoles() {
        return identityRoles;
    }

    public void setIdentityRoles(Set<IdentityRole> identityRoles) {
        this.identityRoles = identityRoles;
    }

}
