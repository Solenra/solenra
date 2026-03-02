package com.github.solenra.server.entity;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "IDENTITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"USERNAME"})})
public class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    private String emailValidationToken;

    private ZonedDateTime emailValidationTimeout;

    private String passwordResetToken;

    private ZonedDateTime passwordResetTimeout;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailValidationToken() {
        return emailValidationToken;
    }

    public void setEmailValidationToken(String emailValidationToken) {
        this.emailValidationToken = emailValidationToken;
    }

    public ZonedDateTime getEmailValidationTimeout() {
        return emailValidationTimeout;
    }

    public void setEmailValidationTimeout(ZonedDateTime emailValidationTimeout) {
        this.emailValidationTimeout = emailValidationTimeout;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public ZonedDateTime getPasswordResetTimeout() {
        return passwordResetTimeout;
    }

    public void setPasswordResetTimeout(ZonedDateTime passwordResetTimeout) {
        this.passwordResetTimeout = passwordResetTimeout;
    }

}
