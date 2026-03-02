package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "INTEGRATION")
public class Integration implements Serializable {

    public static final String CODE_SOLAREDGE_V1 = "solaredge_v1";
    public static final String CODE_SOLAREDGE_V2 = "solaredge_v2";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Boolean enabled;

    private String code;

    private String name;

    @OneToMany(mappedBy = "integration")
    private Set<IntegrationAuthCredential> integrationAuthCredentials;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public Set<IntegrationAuthCredential> getIntegrationAuthCredentials() {
        return integrationAuthCredentials;
    }

    public void setIntegrationAuthCredentials(Set<IntegrationAuthCredential> integrationAuthCredentials) {
        this.integrationAuthCredentials = integrationAuthCredentials;
    }

}
