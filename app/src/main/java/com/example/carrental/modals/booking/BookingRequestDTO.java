package com.example.carrental.modals.booking;

import java.time.LocalDateTime;

public class BookingRequestDTO {
    private Long itemId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BookingRequestDTO(Long itemId, LocalDateTime startTime, LocalDateTime endTime) {
        this.itemId = itemId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}