package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String orderId;
    private List<TicketDTO> tickets;
    private String orderStatus; // e.g., "已完成", "已取消" [cite: 5]
    private Double totalAmount;
    private String message;
}