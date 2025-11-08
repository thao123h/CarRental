package com.example.carrental.modals.auth;

public class UserDTO {
    private Long id;
    private String email;


    private String name;
    private String phone;

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
}
