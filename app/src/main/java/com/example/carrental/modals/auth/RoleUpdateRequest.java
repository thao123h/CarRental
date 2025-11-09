package com.example.carrental.modals.auth;

import com.google.gson.annotations.SerializedName;

public class RoleUpdateRequest {

    private Long userId;
    private String role;

    @SerializedName("deviceID")
    private String deviceID;

    public RoleUpdateRequest(Long userId, String role, String deviceID) {
        this.userId = userId;
        this.role = role;
        this.deviceID = deviceID;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}