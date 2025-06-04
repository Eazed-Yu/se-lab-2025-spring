package com.example.backend.dto;

import lombok.Data;

@Data
public class PurchaseRequestDTO {
    private String userId;
    private String scheduleId;
    private String passengerId; // 乘车人ID
    private String seatType; // e.g., "一等座", "二等座"
}