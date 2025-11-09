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

    private  CarDTO carDTO;
}
