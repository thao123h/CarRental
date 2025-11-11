package com.example.carrental.utils;

import android.content.Context;
import android.graphics.Color;

import com.example.carrental.modals.enums.Status;

/**
 * Helper class to handle Status enum display
 * Provides Vietnamese text and color codes for each status
 */
public class StatusHelper {

    /**
     * Get Vietnamese display text for status
     */
    public static String getStatusText(Status status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case PENDING:
                return "Đang xử lý";
            case CONFIRMED:
                return "Chấp Thuận";
            case NEGOTIATION:
                return "Đang thương lượng";
            case COMPLETED:
                return "Hoàn thành";
            case CANCELLED:
                return "Đã hủy";
            default:
                return status.name();
        }
    }

    /**
     * Get color for status badge background
     * Returns color int value
     */
    public static int getStatusColor(Status status) {
        if (status == null) return Color.parseColor("#9E9E9E"); // Gray

        switch (status) {
            case PENDING:
                return Color.parseColor("#FF9800"); // Orange
            case CONFIRMED:
                return Color.parseColor("#4CAF50"); // Green
            case NEGOTIATION:
                return Color.parseColor("#2196F3"); // Blue
            case COMPLETED:
                return Color.parseColor("#757575"); // Gray
            case CANCELLED:
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.parseColor("#9E9E9E"); // Default Gray
        }
    }

    /**
     * Check if booking can be cancelled by renter
     * Only PENDING and NEGOTIATION bookings can be cancelled
     */
    public static boolean canRenterCancel(Status status) {
        return status == Status.PENDING || status == Status.NEGOTIATION;
    }

    /**
     * Check if booking can be accepted/rejected by owner
     * Only PENDING bookings can be accepted or rejected
     */
    public static boolean canOwnerRespond(Status status) {
        return status == Status.PENDING;
    }
}