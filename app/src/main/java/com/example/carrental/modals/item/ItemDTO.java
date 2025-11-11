package com.example.carrental.modals.item;

import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.enums.AvailabilityStatus;
import com.example.carrental.modals.enums.Category;

import java.time.LocalDateTime;
import java.util.List;

public class ItemDTO {
    private Long id;

    private UserDTO owner;

    private String name;

    private String description;

    private Double price;
    private Double itemValue;
    private Double latePrice;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Double getItemValue() {
        return itemValue;
    }

    public Double getLatePrice() {
        return latePrice;
    }

    public Double getDepositAmount() {
        return depositAmount;
    }

    public String getAddress() {
        return address;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public Category getCategory() {
        return category;
    }

    public List<ItemImageDTO> getItemImages() {
        return itemImages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CarDTO getCarDTO() {
        return carDTO;
    }

    private Double depositAmount;

    private String address;
    private AvailabilityStatus availabilityStatus;

    private Category category;

    private List<ItemImageDTO> itemImages;

    private LocalDateTime createdAt;

    public void setId(Long id) {
        this.id = id;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setItemValue(Double itemValue) {
        this.itemValue = itemValue;
    }

    public void setLatePrice(Double latePrice) {
        this.latePrice = latePrice;
    }

    public void setDepositAmount(Double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setItemImages(List<ItemImageDTO> itemImages) {
        this.itemImages = itemImages;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCarDTO(CarDTO carDTO) {
        this.carDTO = carDTO;
    }


    private  CarDTO carDTO;
}
