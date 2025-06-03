package com.example.backend.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ticket")
public class TicketEntity {
    @TableId
    private String id;
    private String userId;
    private String orderId;
    private String scheduleId;
    private String passengerName;
    private String passengerIdCard;
    private Integer seatTypeId;
    private String carriageNumber;
    private String seatNumber;
    private String ticketType;
    private BigDecimal pricePaid;
    private String ticketStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}