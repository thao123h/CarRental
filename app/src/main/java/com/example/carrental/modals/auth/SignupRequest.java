package com.example.carrental.modals.auth;


import java.util.Set;

public class SignupRequest {

    private String email;
    private String password;
    private String name;
    private Boolean isActive = true;
    private Set<String> stringRoles;

    public SignupRequest() {
    }

    public SignupRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Set<String> getStringRoles() {
        return stringRoles;
    }

    public void setStringRoles(Set<String> stringRoles) {
        this.stringRoles = stringRoles;
    }
}
