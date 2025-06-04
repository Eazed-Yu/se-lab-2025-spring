package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.TicketDTO;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.PassengerMapper;
import com.example.backend.mapper.SeatTypeMapper;
import com.example.backend.mapper.TicketMapper;
import com.example.backend.mapper.TrainScheduleMapper;
import com.example.backend.model.OrderEntity;
import com.example.backend.model.PassengerEntity;
import com.example.backend.model.SeatTypeEntity;
import com.example.backend.model.TicketEntity;
import com.example.backend.model.TrainScheduleEntity;
import com.example.backend.service.OrderService;
import com.example.backend.util.EntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private TrainScheduleMapper trainScheduleMapper;
    
    @Autowired
    private SeatTypeMapper seatTypeMapper;
    
    @Autowired
    private PassengerMapper passengerMapper;

    /**
     * 根据订单ID查询订单
     */
    @Override
    public OrderDTO getOrderById(String orderId) {
        // 查询订单
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        
        // 查询订单关联的车票
        List<TicketEntity> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketEntity>().eq(TicketEntity::getOrderId, orderId)
        );
        
        if (tickets.isEmpty()) {
            return EntityConverter.toOrderDTO(order, new ArrayList<>(), null);
        }
        
        // 查询车票关联的车次和座位类型
        List<String> scheduleIds = tickets.stream()
                .map(TicketEntity::getScheduleId)
                .distinct()
                .collect(Collectors.toList());
        
        List<TrainScheduleEntity> schedules = trainScheduleMapper.selectBatchIds(scheduleIds);
        Map<String, TrainScheduleEntity> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(TrainScheduleEntity::getId, schedule -> schedule));
        
        List<Integer> seatTypeIds = tickets.stream()
                .map(TicketEntity::getSeatTypeId)
                .distinct()
                .collect(Collectors.toList());
        
        List<SeatTypeEntity> seatTypes = seatTypeMapper.selectBatchIds(seatTypeIds);
        Map<Integer, SeatTypeEntity> seatTypeMap = seatTypes.stream()
                .collect(Collectors.toMap(SeatTypeEntity::getId, seatType -> seatType));
        
        // 获取所有相关的乘车人信息
        List<String> passengerIds = tickets.stream()
                .map(TicketEntity::getPassengerId)
                .collect(Collectors.toList());
        List<PassengerEntity> passengers = passengerMapper.selectBatchIds(passengerIds);
        Map<String, PassengerEntity> passengerMap = passengers.stream()
                .collect(Collectors.toMap(PassengerEntity::getId, passenger -> passenger));
        
        // 转换为DTO
        List<TicketDTO> ticketDTOs = tickets.stream()
                .map(ticket -> {
                    TrainScheduleEntity schedule = scheduleMap.get(ticket.getScheduleId());
                    SeatTypeEntity seatType = seatTypeMap.get(ticket.getSeatTypeId());
                    PassengerEntity passenger = passengerMap.get(ticket.getPassengerId());
                    return EntityConverter.toTicketDTO(ticket, schedule, seatType, passenger != null ? passenger.getName() : "未知乘车人");
                })
                .collect(Collectors.toList());
        
        return EntityConverter.toOrderDTO(order, ticketDTOs, null);
    }

    /**
     * 查询用户订单列表
     */
    @Override
    public List<OrderDTO> getUserOrders(String userId) {
        // 查询用户订单
        List<OrderEntity> orders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, userId)
                        .orderByDesc(OrderEntity::getCreateTime)
        );
        
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询订单关联的车票
        List<String> orderIds = orders.stream()
                .map(OrderEntity::getId)
                .collect(Collectors.toList());
        
        List<TicketEntity> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketEntity>()
                        .in(TicketEntity::getOrderId, orderIds)
        );
        
        Map<String, List<TicketEntity>> ticketMap = tickets.stream()
                .collect(Collectors.groupingBy(TicketEntity::getOrderId));
        
        // 查询车票关联的车次和座位类型
        List<String> scheduleIds = tickets.stream()
                .map(TicketEntity::getScheduleId)
                .distinct()
                .collect(Collectors.toList());
        
        List<TrainScheduleEntity> schedules = scheduleIds.isEmpty() ? 
                new ArrayList<>() : trainScheduleMapper.selectBatchIds(scheduleIds);
        Map<String, TrainScheduleEntity> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(TrainScheduleEntity::getId, schedule -> schedule));
        
        List<Integer> seatTypeIds = tickets.stream()
                .map(TicketEntity::getSeatTypeId)
                .distinct()
                .collect(Collectors.toList());
        
        List<SeatTypeEntity> seatTypes = seatTypeIds.isEmpty() ? 
                new ArrayList<>() : seatTypeMapper.selectBatchIds(seatTypeIds);
        Map<Integer, SeatTypeEntity> seatTypeMap = seatTypes.stream()
                .collect(Collectors.toMap(SeatTypeEntity::getId, seatType -> seatType));
        
        // 转换为DTO
        // 获取所有相关的乘车人信息
        List<String> allPassengerIds = tickets.stream()
                .map(TicketEntity::getPassengerId)
                .collect(Collectors.toList());
        List<PassengerEntity> allPassengers = passengerMapper.selectBatchIds(allPassengerIds);
        Map<String, PassengerEntity> allPassengerMap = allPassengers.stream()
                .collect(Collectors.toMap(PassengerEntity::getId, passenger -> passenger));
        
        return orders.stream()
                .map(order -> {
                    List<TicketEntity> orderTickets = ticketMap.getOrDefault(order.getId(), new ArrayList<>());
                    List<TicketDTO> ticketDTOs = orderTickets.stream()
                            .map(ticket -> {
                                TrainScheduleEntity schedule = scheduleMap.get(ticket.getScheduleId());
                                SeatTypeEntity seatType = seatTypeMap.get(ticket.getSeatTypeId());
                                PassengerEntity passenger = allPassengerMap.get(ticket.getPassengerId());
                                return EntityConverter.toTicketDTO(ticket, schedule, seatType, passenger != null ? passenger.getName() : "未知乘车人");
                            })
                            .collect(Collectors.toList());
                    
                    return EntityConverter.toOrderDTO(order, ticketDTOs, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * 创建订单
     */
    @Override
    @Transactional
    public String createOrder(OrderEntity order) {
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        // 保存订单
        orderMapper.insert(order);
        
        return order.getId();
    }

    /**
     * 更新订单状态
     */
    @Override
    public boolean updateOrderStatus(String orderId, String orderStatus, String paymentStatus) {
        // 查询订单
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        
        // 更新订单状态
        order.setOrderStatus(orderStatus);
        order.setPaymentStatus(paymentStatus);
        order.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        
        return true;
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public boolean cancelOrder(String orderId) {
        // 查询订单
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        
        // 检查订单状态是否可以取消
        if ("已完成".equals(order.getOrderStatus()) || "已取消".equals(order.getOrderStatus())) {
            return false;
        }
        
        // 更新订单状态
        order.setOrderStatus("已取消");
        order.setUpdateTime(LocalDateTime.now());
        
        orderMapper.updateById(order);
        
        // 更新车票状态
        List<TicketEntity> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketEntity>().eq(TicketEntity::getOrderId, orderId)
        );
        
        for (TicketEntity ticket : tickets) {
            ticket.setTicketStatus("已取消");
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);
        }
        
        return true;
    }
}