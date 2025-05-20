package com.example.backend.dto;

import com.example.backend.model.TrainSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private String ticketId;
    private TrainSchedule.ScheduleInfoDTO scheduleInfo; // 嵌套车次信息
    private String passengerName;
    private String seatNumber; // e.g., "03车08F座" [cite: 5]
    private String seatType;
    private Double pricePaid;
    private String ticketStatus; // e.g., "已支付", "已退票" [cite: 5, 8]
}