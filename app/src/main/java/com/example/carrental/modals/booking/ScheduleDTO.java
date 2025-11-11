package com.example.carrental.modals.booking;

import java.time.LocalDateTime;

public class ScheduleDTO {
    private LocalDateTime startTime;

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    private LocalDateTime endTime;
}
