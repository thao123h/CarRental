package com.example.carrental.modals.booking;

import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.enums.PaymentStatus;
import com.example.carrental.modals.enums.Status;
import com.example.carrental.modals.item.ItemDTO;

import java.time.LocalDateTime;

/**
 * Booking Response DTO
 * Matches the backend BookingResponseDTO structure
 */
public class BookingResponseDTO {
    private Long id;
    private ItemDTO item;
    private UserDTO renter;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private PaymentStatus paymentStatus;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor
    public BookingResponseDTO() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public ItemDTO getItem() {
        return item;
    }

    public UserDTO getRenter() {
        return renter;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Status getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public void setRenter(UserDTO renter) {
        this.renter = renter;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}