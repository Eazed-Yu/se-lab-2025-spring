package com.example.backend.model;

import com.example.backend.dto.TrainScheduleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainSchedule {
    private String scheduleId; // e.g., G1234_20250520
    private String trainNumber; // e.g., G1234 [cite: 5]
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private Map<SeatType, Integer> seatAvailability; // 座位等级及余票数 [cite: 5]
    private Map<SeatType, Double> basePrice; // 座位等级及基础票价 [cite: 5]
    private String status; // e.g., "正常", "晚点", "取消" [cite: 5]

    // Method to decrease seat count, synchronized for safety
    public synchronized boolean decreaseSeatCount(SeatType seatType, int count) {
        int currentAvailability = seatAvailability.getOrDefault(seatType, 0);
        if (currentAvailability >= count) {
            seatAvailability.put(seatType, currentAvailability - count);
            return true;
        }
        return false;
    }

    // Method to increase seat count, synchronized for safety
    public synchronized void increaseSeatCount(SeatType seatType, int count) {
        seatAvailability.put(seatType, seatAvailability.getOrDefault(seatType, 0) + count);
    }

    public double getPriceForSeat(SeatType seatType) {
        return this.basePrice.getOrDefault(seatType, 0.0);
    }

    // Inner DTO for cleaner representation in responses
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfoDTO {
        private String scheduleId;
        private String trainNumber;
        private String departureStation;
        private String arrivalStation;
        private LocalDateTime departureDateTime;
        private LocalDateTime arrivalDateTime;
    }

    public ScheduleInfoDTO toScheduleInfoDTO() {
        return ScheduleInfoDTO.builder()
                .scheduleId(this.scheduleId)
                .trainNumber(this.trainNumber)
                .departureStation(this.departureStation)
                .arrivalStation(this.arrivalStation)
                .departureDateTime(this.departureDateTime)
                .arrivalDateTime(this.arrivalDateTime)
                .build();
    }
    public TrainScheduleDTO toDetailedDTO() {
        Map<String, Integer> availabilityMap = this.seatAvailability.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDescription(), Map.Entry::getValue));
        Map<String, Double> priceMap = this.basePrice.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDescription(), Map.Entry::getValue));
        long duration = Duration.between(this.departureDateTime, this.arrivalDateTime).toMinutes();

        return TrainScheduleDTO.builder()
                .scheduleId(this.scheduleId)
                .trainNumber(this.trainNumber)
                .departureStation(this.departureStation)
                .arrivalStation(this.arrivalStation)
                .departureDateTime(this.departureDateTime)
                .arrivalDateTime(this.arrivalDateTime)
                .durationMinutes(duration)
                .seatAvailability(availabilityMap)
                .basePrice(priceMap)
                .status(this.status)
                .build();
    }
}