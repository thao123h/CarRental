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

    private String startTime;       // đổi từ LocalDateTime → String
    private String endTime;         // đổi từ LocalDateTime → String
    private Status status;
    private PaymentStatus paymentStatus;
    private String cancellationReason;
    private String createdAt;       // đổi từ LocalDateTime → String
    private String updatedAt;       // đổi từ LocalDateTime → String

    // ==== GETTERS ====
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

}