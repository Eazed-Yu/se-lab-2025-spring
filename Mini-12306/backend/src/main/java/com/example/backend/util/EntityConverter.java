package com.example.backend.util;

import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.PassengerDTO;
import com.example.backend.dto.TicketDTO;
import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.model.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体类与DTO之间的转换工具类
 */
@Component
public class EntityConverter {

    /**
     * 将TrainScheduleEntity和SeatAvailability列表转换为TrainScheduleDTO
     */
    public static TrainScheduleDTO toTrainScheduleDTO(TrainScheduleEntity entity, List<SeatAvailabilityEntity> availabilities, List<SeatTypeEntity> seatTypes) {
        Map<String, Integer> availabilityMap = new HashMap<>();
        Map<String, Double> priceMap = new HashMap<>();
        
        // 构建座位可用性和价格映射
        for (SeatAvailabilityEntity availability : availabilities) {
            // 查找对应的座位类型
            SeatTypeEntity seatType = seatTypes.stream()
                    .filter(type -> type.getId().equals(availability.getSeatTypeId()))
                    .findFirst()
                    .orElse(null);
            
            if (seatType != null) {
                availabilityMap.put(seatType.getName(), availability.getAvailableCount());
                priceMap.put(seatType.getName(), availability.getPrice().doubleValue());
            }
        }
        
        // 计算行程时间（分钟）
        long durationMinutes = Duration.between(entity.getDepartureTime(), entity.getArrivalTime()).toMinutes();
        
        return TrainScheduleDTO.builder()
                .scheduleId(entity.getId())
                .trainNumber(entity.getTrainNumber())
                .departureStation(entity.getDepartureStation())
                .arrivalStation(entity.getArrivalStation())
                .departureDateTime(entity.getDepartureTime())
                .arrivalDateTime(entity.getArrivalTime())
                .durationMinutes(durationMinutes)
                .seatAvailability(availabilityMap)
                .basePrice(priceMap)
                .status(entity.getStatus())
                .build();
    }
    
    /**
     * 将TrainScheduleEntity转换为TrainSchedule.ScheduleInfoDTO
     */
    public static TrainSchedule.ScheduleInfoDTO toScheduleInfoDTO(TrainScheduleEntity entity) {
        return TrainSchedule.ScheduleInfoDTO.builder()
                .scheduleId(entity.getId())
                .trainNumber(entity.getTrainNumber())
                .departureStation(entity.getDepartureStation())
                .arrivalStation(entity.getArrivalStation())
                .departureDateTime(entity.getDepartureTime())
                .arrivalDateTime(entity.getArrivalTime())
                .build();
    }
    
    /**
     * 将TicketEntity、TrainScheduleEntity和SeatTypeEntity转换为TicketDTO
     */
    public static TicketDTO toTicketDTO(TicketEntity ticket, TrainScheduleEntity schedule, SeatTypeEntity seatType, String passengerName) {
        return TicketDTO.builder()
                .ticketId(ticket.getId())
                .scheduleInfo(toScheduleInfoDTO(schedule))
                .passengerName(passengerName)
                .seatNumber(ticket.getSeatNumber())
                .seatType(seatType.getName())
                .pricePaid(ticket.getPricePaid().doubleValue())
                .ticketStatus(ticket.getTicketStatus())
                .build();
    }
    
    /**
     * 将OrderEntity和TicketDTO列表转换为OrderDTO
     */
    public static OrderDTO toOrderDTO(OrderEntity order, List<TicketDTO> tickets, String message) {
        return OrderDTO.builder()
                .orderId(order.getId())
                .tickets(tickets)
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount().doubleValue())
                .message(message)
                .build();
    }
    
    /**
     * 将PassengerEntity转换为PassengerDTO
     */
    public PassengerDTO convertToPassengerDTO(PassengerEntity entity) {
        if (entity == null) {
            return null;
        }
        
        PassengerDTO dto = new PassengerDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setIdCard(entity.getIdCard());
        dto.setPhone(entity.getPhone());
        dto.setIdCardPhotoPath(entity.getIdCardPhotoPath());
        dto.setIsDefault(entity.getIsDefault());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        
        return dto;
    }
}