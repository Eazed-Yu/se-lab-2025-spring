package com.example.backend.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatType {
    FIRST_CLASS("一等座", 800.0),
    SECOND_CLASS("二等座", 550.0),
    BUSINESS_CLASS("商务座", 1200.0);

    private final String description;
    private final double basePrice;

    SeatType(String description, double basePrice) {
        this.description = description;
        this.basePrice = basePrice;
    }

    public static SeatType fromDescription(String seatType) {
        for (SeatType type : SeatType.values()) {
            if (type.getDescription().equals(seatType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown seat type: " + seatType);
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    // 移除冗余方法，使用上面的fromDescription方法即可
}