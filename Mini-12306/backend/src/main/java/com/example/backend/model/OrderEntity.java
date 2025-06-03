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
@TableName("`order`")
public class OrderEntity {
    @TableId
    private String id;
    private String userId;
    private String orderType;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}