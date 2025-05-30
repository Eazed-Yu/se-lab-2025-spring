package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String userId;
    private List<String> ticketIds;
    private LocalDateTime orderCreationTime;
    private String orderType; // "购票", "退票", "改签" [cite: 5]
    private Double totalAmount;
    private String paymentStatus; // "未支付", "支付成功", "支付失败" [cite: 5]
    private String orderStatus; // "处理中", "已完成", "已取消" [cite: 5]

    public enum Status {
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;
        Status(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum PaymentStatus {
        UNPAID("未支付"),
        SUCCESS("支付成功"),
        FAILED("支付失败");

        private final String description;
        PaymentStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}