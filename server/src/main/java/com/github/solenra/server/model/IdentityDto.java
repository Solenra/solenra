package com.github.solenra.server.model;

public class IdentityDto {

    private String username;

    public IdentityDto() {
    }

    public IdentityDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
