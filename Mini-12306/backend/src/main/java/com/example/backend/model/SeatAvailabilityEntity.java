package com.example.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("seat_availability")
public class SeatAvailabilityEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String scheduleId;
    private Integer seatTypeId;
    private Integer availableCount;
    private BigDecimal price;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}