package com.example.carrental.modals.item;

import com.example.carrental.modals.enums.FuelType;
import com.example.carrental.modals.enums.Transmission;

import java.util.List;

public class CarDTO {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private Transmission transmission;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getKms() {
        return kms;
    }

    public void setKms(Integer kms) {
        this.kms = kms;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public List<ItemImageDTO> getItemImages() {
        return itemImages;
    }

    public void setItemImages(List<ItemImageDTO> itemImages) {
        this.itemImages = itemImages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CarDTO(Long id, String brand, String model, Integer year, Transmission transmission, FuelType fuelType, Integer seats, String licensePlate, Integer kms, ItemDTO item, List<ItemImageDTO> itemImages) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.seats = seats;
        this.licensePlate = licensePlate;
        this.kms = kms;
        this.item = item;
        this.itemImages = itemImages;
    }

    private FuelType fuelType;
    private Integer seats;
    private String licensePlate;
    private Integer kms;
    private ItemDTO item;
    private List<ItemImageDTO> itemImages;
}
