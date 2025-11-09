package com.example.carrental.modals.item;

import com.example.carrental.modals.enums.FuelType;
import com.example.carrental.modals.enums.Transmission;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CarDTO {

    // ✅ Constructor mặc định cho Gson
    public CarDTO() {}

    // ✅ Các trường với @SerializedName để ánh xạ JSON chính xác
    @SerializedName("id")
    private Long id;

    @SerializedName("brand")
    private String brand;

    @SerializedName("model")
    private String model;

    @SerializedName("year")
    private Integer year;

    @SerializedName("transmission")
    private Transmission transmission;

    @SerializedName("fuelType")
    private FuelType fuelType;

    @SerializedName("seats")
    private Integer seats;

    @SerializedName("licensePlate")
    private String licensePlate;

    @SerializedName("kms")
    private Integer kms;

    @SerializedName("item")
    private ItemDTO item;

    @SerializedName("itemImages")
    private List<ItemImageDTO> itemImages;

    // ✅ Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
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
}
