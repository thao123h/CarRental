package com.example.carrental.modals.booking;

public class BookingRequestDTO {
    private Long itemId;
    private String startTime;
    private String endTime;

    public BookingRequestDTO(Long itemId, String startTime, String endTime) {
        this.itemId = itemId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
