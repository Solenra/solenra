package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "IDENTITY_ROLE", uniqueConstraints = {@UniqueConstraint(columnNames = {"IDENTITY_ID", "ROLE_ID"})})
public class IdentityRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "IDENTITY_ID", nullable = false)
    private Identity identity;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private Role role;

    public IdentityRole() {
    }

    public IdentityRole(Identity identity, Role role) {
        this.identity = identity;
        this.role = role;
    }

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

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
