package com.example.carrental.modals.auth;

public class LoginRequest {

    private String email;
    private String password;

    private String deviceId;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, String deviceId) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
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
}
