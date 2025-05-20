package com.example.backend.dto;

import lombok.Data;

@Data
public class PurchaseRequestDTO {
    private String userId;
    private String scheduleId;
    private String passengerName;
    private String passengerIdCard;
    private String seatType; // e.g., "一等座", "二等座" [cite: 5]
}