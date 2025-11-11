package com.example.carrental.modals.auth;

public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String phone;

    public UserDTO() {
    }
    public UserDTO(Long id, String email, String name, String phone, String identityCard, String address, String licenseNumber, int tokenVersion) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.identityCard = identityCard;
        this.address = address;
        this.licenseNumber = licenseNumber;
        this.tokenVersion = tokenVersion;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public String getAddress() {
        return address;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    private String address;
    private String identityCard;
    private String licenseNumber;
    private int tokenVersion;
}
