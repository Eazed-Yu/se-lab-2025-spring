package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private String ticketId;
    private String orderId;
    private String scheduleId;
    private String passengerName; // 乘车人姓名 [cite: 5]
    private String passengerIdCard; // 乘车人身份证号 [cite: 5]
    private String seatNumber; // e.g., "01车08A号" [cite: 5]
    private String carriageNumber; // 车厢号 [cite: 5]
    private SeatType seatType;
    private String ticketType; // e.g., "成人票", "学生票" [cite: 5]
    private Double pricePaid;
    private String ticketStatus; // "待支付", "已支付", "已出票", "已检票", "已退票", "已改签", "已取消" [cite: 8]

    public enum Status {
        UNPAID("待支付"),
        PAID("已支付"),
        ISSUED("已出票"),
        CHECKED_IN("已检票"),
        REFUNDED("已退票"),
        REBOOKED("已改签"),
        CANCELLED("已取消");

        private final String description;
        Status(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}