package com.example.carrental.modals.booking;

import com.example.carrental.modals.enums.PaymentStatus;
import com.example.carrental.modals.enums.Status;

public class UpdateBookingRequest {
    public UpdateBookingRequest(Status status, String cancellationReason, PaymentStatus paymentStatus) {
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.paymentStatus = paymentStatus;
    }

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    private PaymentStatus paymentStatus;
    private String cancellationReason;
}
