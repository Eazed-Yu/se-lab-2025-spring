package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainScheduleDTO {
    private String scheduleId; // 车次ID
    private String trainNumber; // 火车编号
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private Long durationMinutes;
    private Map<String, Integer> seatAvailability; // 使用 String 作为 Key 以便 JSON 序列化
    private Map<String, Double> basePrice;      // 使用 String 作为 Key
    private String status;
}