package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.dto.*;
import com.example.backend.mapper.*;
import com.example.backend.model.*;
import com.example.backend.service.IdentityVerificationService;
import com.example.backend.service.PaymentService;
import com.example.backend.service.TicketService;
import com.example.backend.util.EntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TrainScheduleMapper trainScheduleMapper;
    
    @Autowired
    private SeatTypeMapper seatTypeMapper;
    
    @Autowired
    private SeatAvailabilityMapper seatAvailabilityMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private PassengerMapper passengerMapper;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IdentityVerificationService identityVerificationService;

    /**
     * 查询车次时刻表
     */
    @Override
    public List<TrainScheduleDTO> querySchedules(String departureStation, String arrivalStation, String departureDateStr) {
        LocalDate departureDate = null;
        if (StringUtils.hasText(departureDateStr)) {
            try {
                departureDate = LocalDate.parse(departureDateStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("日期格式错误，请使用 YYYY-MM-DD 格式。");
            }
        }

        // 构建查询条件
        QueryWrapper<TrainScheduleEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(departureStation)) {
            queryWrapper.eq("departure_station", departureStation);
        }
        if (StringUtils.hasText(arrivalStation)) {
            queryWrapper.eq("arrival_station", arrivalStation);
        }
        if (departureDate != null) {
            queryWrapper.ge("departure_time", departureDate.atStartOfDay());
            queryWrapper.lt("departure_time", departureDate.plusDays(1).atStartOfDay());
        }

        // 查询车次
        List<TrainScheduleEntity> schedules = trainScheduleMapper.selectList(queryWrapper);
        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有座位类型
        List<SeatTypeEntity> seatTypes = seatTypeMapper.selectList(null);

        // 查询每个车次的座位可用性
        List<TrainScheduleDTO> result = new ArrayList<>();
        for (TrainScheduleEntity schedule : schedules) {
            List<SeatAvailabilityEntity> availabilities = seatAvailabilityMapper.selectList(
                    new QueryWrapper<SeatAvailabilityEntity>().eq("schedule_id", schedule.getId())
            );
            result.add(EntityConverter.toTrainScheduleDTO(schedule, availabilities, seatTypes));
        }

        return result;
    }

    /**
     * 购买火车票
     */
    @Override
    @Transactional
    public OrderDTO purchaseTicket(PurchaseRequestDTO request) {
        // 1. 查询车次信息
        TrainScheduleEntity schedule = trainScheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("车次信息未找到：" + request.getScheduleId());
        }

        // 2. 查询座位类型
        SeatTypeEntity seatType = getSeatTypeByName(request.getSeatType());
        if (seatType == null) {
            throw new IllegalArgumentException("无效的座位类型：" + request.getSeatType());
        }

        // 3. 查询座位可用性
        SeatAvailabilityEntity seatAvailability = getSeatAvailability(request.getScheduleId(), seatType.getId());
        if (seatAvailability == null || seatAvailability.getAvailableCount() <= 0) {
            throw new RuntimeException("所选车次座位余票不足。");
        }

        // 4. 查询乘车人信息
        PassengerEntity passenger = passengerMapper.selectById(request.getPassengerId());
        if (passenger == null) {
            throw new IllegalArgumentException("乘车人信息未找到：" + request.getPassengerId());
        }
        
        // 验证乘车人归属
        if (!passenger.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("无权使用此乘车人信息。");
        }
        
        // 身份信息核验
        boolean identityVerified = identityVerificationService.verifyIdentity(passenger.getName(), passenger.getIdCard());
        if (!identityVerified) {
            throw new RuntimeException("身份信息核验失败。");
        }

        // 5. 创建订单和车票 (初始状态：待支付)
        String orderId = UUID.randomUUID().toString().replace("-", "");
        String ticketId = UUID.randomUUID().toString().replace("-", "");

        // 创建订单
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .userId(request.getUserId())
                .orderType("购票")
                .totalAmount(seatAvailability.getPrice())
                .paymentStatus("未支付")
                .orderStatus("处理中")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 创建车票
        TicketEntity ticket = TicketEntity.builder()
                .id(ticketId)
                .userId(request.getUserId())
                .orderId(orderId)
                .scheduleId(request.getScheduleId())
                .passengerId(request.getPassengerId())
                .seatTypeId(seatType.getId())
                .ticketType("成人票") // 简化处理，实际应根据乘客信息确定票种
                .pricePaid(seatAvailability.getPrice())
                .seatNumber("待分配") // 简化处理，在支付成功后分配座位号
                .ticketStatus("待支付")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 保存订单和车票
        orderMapper.insert(order);
        ticketMapper.insert(ticket);

        // 6. 调用支付服务
        boolean paymentSuccessful = paymentService.processPayment(orderId, seatAvailability.getPrice().doubleValue());

        if (paymentSuccessful) {
            // 7. 支付成功: 锁定座位，更新状态，生成票号等
            // 减少座位数量
            seatAvailability.setAvailableCount(seatAvailability.getAvailableCount() - 1);
            seatAvailabilityMapper.updateById(seatAvailability);

            // 分配座位号
            String seatNumber = assignSeat(schedule.getTrainNumber(), seatType.getName());
            ticket.setSeatNumber(seatNumber);
            ticket.setTicketStatus("已支付");
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);

            // 更新订单状态
            order.setPaymentStatus("支付成功");
            order.setOrderStatus("已完成");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 构建返回结果
            TicketDTO ticketDTO = EntityConverter.toTicketDTO(ticket, schedule, seatType, passenger.getName());
            return EntityConverter.toOrderDTO(order, Collections.singletonList(ticketDTO), "购票成功");
        } else {
            // 8. 支付失败: 更新状态为取消
            ticket.setTicketStatus("已取消");
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);

            order.setPaymentStatus("支付失败");
            order.setOrderStatus("已取消");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            throw new RuntimeException("支付失败，订单已取消。");
        }
    }

    /**
     * 退票业务流程实现
     */
    @Override
    @Transactional
    public RefundResponseDTO refundTicket(RefundRequestDTO request) {
        // 1. 查询车票信息
        TicketEntity ticket = ticketMapper.selectById(request.getTicketId());
        if (ticket == null) {
            throw new IllegalArgumentException("车票不存在：" + request.getTicketId());
        }

        // 2. 检查退票条件
        if (!"已支付".equals(ticket.getTicketStatus()) && !"已出票".equals(ticket.getTicketStatus())) {
            throw new RuntimeException("当前车票状态不符合退票条件：" + ticket.getTicketStatus());
        }

        // 3. 查询订单信息
        OrderEntity order = orderMapper.selectById(ticket.getOrderId());
        if (order == null) {
            throw new IllegalStateException("订单不存在：" + ticket.getOrderId());
        }

        // 4. 调用支付服务处理退款
        boolean refundSuccessful = paymentService.processRefund(ticket.getId(), ticket.getPricePaid().doubleValue());
        if (!refundSuccessful) {
            throw new RuntimeException("退款处理失败。");
        }

        // 5. 更新车票状态
        ticket.setTicketStatus("已退票");
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        // 6. 更新订单状态
        if ("购票".equals(order.getOrderType())) {
            order.setOrderType("退票");
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 7. 释放座位资源
        SeatAvailabilityEntity seatAvailability = getSeatAvailability(ticket.getScheduleId(), ticket.getSeatTypeId());
        if (seatAvailability != null) {
            seatAvailability.setAvailableCount(seatAvailability.getAvailableCount() + 1);
            seatAvailabilityMapper.updateById(seatAvailability);
        }

        return new RefundResponseDTO(ticket.getId(), "退票成功，款项将原路退回。", "已退票");
    }

    /**
     * 改签车票业务流程实现
     */
    @Override
    @Transactional
    public ChangeTicketResponseDTO changeTicket(ChangeTicketRequestDTO request) {
        // 1. 查询原车票信息
        TicketEntity oldTicket = ticketMapper.selectById(request.getTicketId());
        if (oldTicket == null) {
            throw new IllegalArgumentException("车票不存在：" + request.getTicketId());
        }

        // 2. 检查改签条件
        if (!"已支付".equals(oldTicket.getTicketStatus()) && !"已出票".equals(oldTicket.getTicketStatus())) {
            throw new RuntimeException("当前车票状态不符合改签条件：" + oldTicket.getTicketStatus());
        }

        // 3. 查询新车次信息
        TrainScheduleEntity newSchedule = trainScheduleMapper.selectById(request.getNewScheduleId());
        if (newSchedule == null) {
            throw new IllegalArgumentException("目标车次不存在：" + request.getNewScheduleId());
        }

        // 4. 查询座位类型
        SeatTypeEntity seatType = getSeatTypeByName(request.getSeatType());
        if (seatType == null) {
            throw new IllegalArgumentException("无效的座位类型：" + request.getSeatType());
        }

        // 5. 查询新车次座位可用性
        SeatAvailabilityEntity newSeatAvailability = getSeatAvailability(request.getNewScheduleId(), seatType.getId());
        if (newSeatAvailability == null || newSeatAvailability.getAvailableCount() <= 0) {
            throw new RuntimeException("目标车次座位余票不足。");
        }

        // 6. 计算差价
        BigDecimal priceDifference = newSeatAvailability.getPrice().subtract(oldTicket.getPricePaid());

        // 7. 创建新订单和车票
        String newOrderId = UUID.randomUUID().toString().replace("-", "");
        String newTicketId = UUID.randomUUID().toString().replace("-", "");

        // 创建新订单
        OrderEntity newOrder = OrderEntity.builder()
                .id(newOrderId)
                .userId(oldTicket.getUserId())
                .orderType("改签")
                .totalAmount(priceDifference.compareTo(BigDecimal.ZERO) > 0 ? priceDifference : BigDecimal.ZERO)
                .paymentStatus(priceDifference.compareTo(BigDecimal.ZERO) > 0 ? "未支付" : "支付成功")
                .orderStatus("处理中")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 创建新车票
        TicketEntity newTicket = TicketEntity.builder()
                .id(newTicketId)
                .userId(oldTicket.getUserId())
                .orderId(newOrderId)
                .scheduleId(request.getNewScheduleId())
                .passengerId(oldTicket.getPassengerId())
                .seatTypeId(seatType.getId())
                .ticketType(oldTicket.getTicketType())
                .pricePaid(newSeatAvailability.getPrice())
                .seatNumber("待分配")
                .ticketStatus(priceDifference.compareTo(BigDecimal.ZERO) > 0 ? "待支付" : "已支付")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 保存新订单和车票
        orderMapper.insert(newOrder);
        ticketMapper.insert(newTicket);

        // 8. 处理差价支付
        boolean paymentSuccessful = true;
        if (priceDifference.compareTo(BigDecimal.ZERO) > 0) {
            // 需要补差价
            paymentSuccessful = paymentService.processPayment(newOrderId, priceDifference.doubleValue());
        }

        if (paymentSuccessful) {
            // 9. 支付成功或无需支付: 更新状态，分配座位
            // 更新原车票状态
            oldTicket.setTicketStatus("已改签");
            oldTicket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(oldTicket);

            // 释放原座位
            SeatAvailabilityEntity oldSeatAvailability = getSeatAvailability(oldTicket.getScheduleId(), oldTicket.getSeatTypeId());
            if (oldSeatAvailability != null) {
                oldSeatAvailability.setAvailableCount(oldSeatAvailability.getAvailableCount() + 1);
                seatAvailabilityMapper.updateById(oldSeatAvailability);
            }

            // 锁定新座位
            newSeatAvailability.setAvailableCount(newSeatAvailability.getAvailableCount() - 1);
            seatAvailabilityMapper.updateById(newSeatAvailability);

            // 分配新座位号
            String seatNumber = assignSeat(newSchedule.getTrainNumber(), seatType.getName());
            newTicket.setSeatNumber(seatNumber);
            newTicket.setTicketStatus("已支付");
            newTicket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(newTicket);

            // 更新新订单状态
            newOrder.setPaymentStatus("支付成功");
            newOrder.setOrderStatus("已完成");
            newOrder.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(newOrder);

            // 获取乘车人信息
            PassengerEntity passenger = passengerMapper.selectById(newTicket.getPassengerId());
            
            // 构建返回结果
            TicketDTO newTicketDTO = EntityConverter.toTicketDTO(newTicket, newSchedule, seatType, passenger.getName());
            return new ChangeTicketResponseDTO(oldTicket.getId(), newTicket.getId(), "改签成功", newTicketDTO);
        } else {
            // 10. 支付失败: 取消改签
            newTicket.setTicketStatus("已取消");
            newTicket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(newTicket);

            newOrder.setPaymentStatus("支付失败");
            newOrder.setOrderStatus("已取消");
            newOrder.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(newOrder);

            throw new RuntimeException("支付差价失败，改签已取消。");
        }
    }

    /**
     * 查询用户车票
     */
    @Override
    public List<TicketDTO> getUserTickets(String userId) {
        // 查询用户的所有车票
        List<TicketEntity> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketEntity>()
                        .eq(TicketEntity::getUserId, userId)
                        .orderByDesc(TicketEntity::getCreateTime)
        );

        if (tickets.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有座位类型
        List<SeatTypeEntity> seatTypes = seatTypeMapper.selectList(null);
        Map<Integer, SeatTypeEntity> seatTypeMap = seatTypes.stream()
                .collect(Collectors.toMap(SeatTypeEntity::getId, seatType -> seatType));

        // 获取所有相关的车次信息
        Set<String> scheduleIds = tickets.stream()
                .map(TicketEntity::getScheduleId)
                .collect(Collectors.toSet());
        List<TrainScheduleEntity> schedules = trainScheduleMapper.selectBatchIds(scheduleIds);
        Map<String, TrainScheduleEntity> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(TrainScheduleEntity::getId, schedule -> schedule));

        // 获取所有相关的乘车人信息
        Set<String> passengerIds = tickets.stream()
                .map(TicketEntity::getPassengerId)
                .collect(Collectors.toSet());
        List<PassengerEntity> passengers = passengerMapper.selectBatchIds(passengerIds);
        Map<String, PassengerEntity> passengerMap = passengers.stream()
                .collect(Collectors.toMap(PassengerEntity::getId, passenger -> passenger));

        // 转换为DTO
        return tickets.stream()
                .map(ticket -> {
                    TrainScheduleEntity schedule = scheduleMap.get(ticket.getScheduleId());
                    SeatTypeEntity seatType = seatTypeMap.get(ticket.getSeatTypeId());
                    PassengerEntity passenger = passengerMap.get(ticket.getPassengerId());
                    if (schedule != null && seatType != null && passenger != null) {
                        return EntityConverter.toTicketDTO(ticket, schedule, seatType, passenger.getName());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 根据座位类型名称获取座位类型实体
     */
    private SeatTypeEntity getSeatTypeByName(String seatTypeName) {
        return seatTypeMapper.selectOne(
                new QueryWrapper<SeatTypeEntity>().eq("name", seatTypeName)
        );
    }

    /**
     * 获取座位可用性
     */
    private SeatAvailabilityEntity getSeatAvailability(String scheduleId, Integer seatTypeId) {
        return seatAvailabilityMapper.selectOne(
                new QueryWrapper<SeatAvailabilityEntity>()
                        .eq("schedule_id", scheduleId)
                        .eq("seat_type_id", seatTypeId)
        );
    }

    /**
     * 模拟分配座位号
     */
    private String assignSeat(String trainNumber, String seatType) {
        // 简化实现，随机生成座位号
        Random random = new Random();
        int carriageNumber = random.nextInt(16) + 1; // 1-16号车厢
        char seatRow = (char) (random.nextInt(26) + 'A'); // A-Z排
        int seatColumn = random.nextInt(5) + 1; // 1-5号座位
        
        return String.format("%02d车%d%c号", carriageNumber, seatColumn, seatRow);
    }
}