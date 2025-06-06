package com.example.backend.dto;

import lombok.Data;

@Data
public class PurchaseRequestDTO {
    private String userId; // 用户ID
    private String scheduleId; // 车次ID
    private String passengerId; // 乘车人ID
    private String seatType; // e.g., "一等座", "二等座"
}