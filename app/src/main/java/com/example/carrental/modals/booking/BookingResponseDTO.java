package com.example.carrental.modals.booking;

import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.enums.PaymentStatus;
import com.example.carrental.modals.enums.Status;
import com.example.carrental.modals.item.ItemDTO;

import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private ItemDTO item;
    private UserDTO renter;

    public Long getId() {
        return id;
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

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private PaymentStatus paymentStatus;

    private String cancellationReason;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}