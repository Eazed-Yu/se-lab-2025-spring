package com.example.backend.service;
import com.example.backend.dto.*;
import com.example.backend.model.Order;
import com.example.backend.model.SeatType;
import com.example.backend.model.Ticket;
import com.example.backend.model.TrainSchedule;
import com.example.backend.store.MockDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private MockDataStore dataStore;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IdentityVerificationService identityVerificationService;

    /**
     * 查询车次时刻表
     * @param departureStation 出发站 (可选)
     * @param arrivalStation 到达站 (可选)
     * @param departureDateStr 出发日期 (可选, YYYY-MM-DD)
     * @return 符合条件的车次列表
     */
    public List<TrainScheduleDTO> querySchedules(String departureStation, String arrivalStation, String departureDateStr) {
        LocalDate departureDate = null;
        if (StringUtils.hasText(departureDateStr)) {
            try {
                departureDate = LocalDate.parse(departureDateStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("日期格式错误，请使用 YYYY-MM-DD 格式。");
            }
        }

        final LocalDate finalDepartureDate = departureDate;

        return dataStore.trainSchedules.values().stream()
                .filter(schedule -> !StringUtils.hasText(departureStation) || schedule.getDepartureStation().equalsIgnoreCase(departureStation))
                .filter(schedule -> !StringUtils.hasText(arrivalStation) || schedule.getArrivalStation().equalsIgnoreCase(arrivalStation))
                .filter(schedule -> finalDepartureDate == null || schedule.getDepartureDateTime().toLocalDate().equals(finalDepartureDate))
                .map(TrainSchedule::toDetailedDTO)
                .collect(Collectors.toList());
    }


    /**
     * 购买火车票业务流程实现
     * 
     * 完整流程：
     * 1. 查询车次信息并验证座位类型
     * 2. 验证乘客身份信息
     * 3. 检查余票并锁定车票（原子操作）
     * 4. 创建订单和车票（初始状态为待支付）
     * 5. 调用支付服务进行支付
     * 6. 根据支付结果更新订单和车票状态
     * 
     * @param request 购票请求DTO，包含用户信息、车次信息和座位类型等
     * @return 订单DTO，包含订单信息和车票详情
     * @throws IllegalArgumentException 当车次不存在或座位类型无效时
     * @throws RuntimeException 当身份验证失败、余票不足或支付失败时
     */
    public OrderDTO purchaseTicket(PurchaseRequestDTO request) {
        // 1. 查询车次信息
        TrainSchedule schedule = dataStore.trainSchedules.get(request.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("车次信息未找到：" + request.getScheduleId());
        }

        SeatType requestedSeatType;
        try {
            requestedSeatType = SeatType.fromDescription(request.getSeatType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的座位类型：" + request.getSeatType());
        }

        double price = schedule.getPriceForSeat(requestedSeatType);
        if (price == 0.0) {
            throw new IllegalArgumentException("该车次不提供座位类型：" + request.getSeatType());
        }

        // 2. 身份信息核验
        boolean identityVerified = identityVerificationService.verifyIdentity(request.getPassengerName(), request.getPassengerIdCard());
        if (!identityVerified) {
            throw new RuntimeException("身份信息核验失败。");
        }

        // 3. 检查余票 (原子操作)
        synchronized (schedule) {
            // 只检查余票，还不锁定
            int availableSeats = schedule.getAvailableSeats(requestedSeatType);
            if (availableSeats <= 0) {
                throw new RuntimeException("所选车次座位余票不足。");
            }

            // 4. 创建订单和车票 (初始状态：待支付)
            String orderId = dataStore.generateOrderId();
            String ticketId = dataStore.generateTicketId();

            Ticket newTicket = Ticket.builder()
                    .ticketId(ticketId)
                    .userId(request.getUserId())
                    .orderId(orderId)
                    .scheduleId(request.getScheduleId())
                    .passengerName(request.getPassengerName())
                    .passengerIdCard(request.getPassengerIdCard())
                    .seatType(requestedSeatType)
                    .ticketType("成人票") // 简化处理，实际应根据乘客信息确定票种
                    .pricePaid(price)
                    .seatNumber("待分配") // 简化处理，在支付成功后分配座位号
                    .ticketStatus(Ticket.Status.UNPAID.getDescription())
                    .build();

            Order newOrder = Order.builder()
                    .orderId(orderId)
                    .userId(request.getUserId())
                    .ticketIds(Collections.singletonList(ticketId))
                    .orderCreationTime(LocalDateTime.now())
                    .orderType("购票")
                    .totalAmount(price)
                    .paymentStatus(Order.PaymentStatus.UNPAID.getDescription())
                    .orderStatus(Order.Status.PROCESSING.getDescription())
                    .build();

            dataStore.tickets.put(ticketId, newTicket);
            dataStore.orders.put(orderId, newOrder);

            // 5. 调用支付服务
            boolean paymentSuccessful = paymentService.processPayment(orderId, newOrder.getTotalAmount());

            if (paymentSuccessful) {
                // 6. 支付成功: 锁定座位，更新状态，生成票号等
                // 再次检查并锁定座位
                if (!schedule.decreaseSeatCount(requestedSeatType, 1)) {
                    // 如果此时没有余票了，则退款并取消订单
                    paymentService.processRefund(ticketId, price);
                    newTicket.setTicketStatus(Ticket.Status.CANCELLED.getDescription());
                    newOrder.setPaymentStatus("已退款"); 
                    newOrder.setOrderStatus(Order.Status.CANCELLED.getDescription());
                    
                    // 更新数据存储
                    dataStore.tickets.put(ticketId, newTicket);
                    dataStore.orders.put(orderId, newOrder);
                    throw new RuntimeException("支付成功，但座位已被抢占，款项将原路退回。");
                }

                newTicket.setTicketStatus(Ticket.Status.PAID.getDescription());
                newTicket.setSeatNumber(assignSeat(schedule.getTrainNumber(), requestedSeatType.getDescription())); // 模拟座位分配
                newOrder.setPaymentStatus(Order.PaymentStatus.SUCCESS.getDescription());
                newOrder.setOrderStatus(Order.Status.COMPLETED.getDescription());

                // 更新数据存储
                dataStore.tickets.put(ticketId, newTicket);
                dataStore.orders.put(orderId, newOrder);

                return convertToOrderDTO(newOrder, Collections.singletonList(newTicket), "购票成功");
            } else {
                // 7. 支付失败: 更新状态为取消
                newTicket.setTicketStatus(Ticket.Status.CANCELLED.getDescription());
                newOrder.setPaymentStatus(Order.PaymentStatus.FAILED.getDescription());
                newOrder.setOrderStatus(Order.Status.CANCELLED.getDescription());

                // 更新数据存储
                dataStore.tickets.put(ticketId, newTicket);
                dataStore.orders.put(orderId, newOrder);
                throw new RuntimeException("支付失败，订单已取消。");
            }
        }
    }

    /**
     * 退票业务流程实现
     * 
     * 完整流程：
     * 1. 查询车票信息和订单信息
     * 2. 检查退票条件（车票状态是否符合退票要求）
     * 3. 调用支付服务处理退款
     * 4. 更新车票状态和订单状态
     * 5. 释放座位资源（增加余票数量）
     * 
     * @param request 退票请求DTO，包含用户ID和车票ID
     * @return 退票响应DTO，包含退票结果信息
     * @throws IllegalArgumentException 当车票不存在时
     * @throws IllegalStateException 当订单不存在时（数据不一致）
     * @throws RuntimeException 当不符合退票条件或退款处理失败时
     */
    public RefundResponseDTO refundTicket(RefundRequestDTO request) {
        Ticket ticket = dataStore.tickets.get(request.getTicketId());
        if (ticket == null) {
            throw new IllegalArgumentException("车票信息未找到：" + request.getTicketId());
        }

        Order order = dataStore.orders.get(ticket.getOrderId());
        if (order == null) {
            throw new IllegalStateException("关联订单未找到，数据不一致：" + ticket.getOrderId());
        }

        // 1. 检查退票规则（简化处理）
        // 演示：只有"已支付"或"已出票"状态的车票可以退票
        // 简化处理：不考虑发车时间限制
        if (!(ticket.getTicketStatus().equals(Ticket.Status.PAID.getDescription()) ||
                ticket.getTicketStatus().equals(Ticket.Status.ISSUED.getDescription()))) {
            throw new RuntimeException("当前车票状态不符合退票条件：" + ticket.getTicketStatus());
        }

        // 修改退票流程顺序，先标记票为退票处理中，释放座位，然后尝试退款

        // 1. 标记车票状态为退票处理中
        String originalStatus = ticket.getTicketStatus(); // 备份原始状态，以便退款失败时恢复
        ticket.setTicketStatus("退票处理中");
        dataStore.tickets.put(ticket.getTicketId(), ticket);
        
        // 2. 释放座位资源
        TrainSchedule schedule = dataStore.trainSchedules.get(ticket.getScheduleId());
        if (schedule != null) {
            schedule.increaseSeatCount(ticket.getSeatType(), 1);
        }

        // 3. 调用支付服务处理退款
        boolean refundProcessed = paymentService.processRefund(ticket.getTicketId(), ticket.getPricePaid());

        if (refundProcessed) {
            // 4. 退款成功，更新车票和订单状态
            ticket.setTicketStatus(Ticket.Status.REFUNDED.getDescription());
            dataStore.tickets.put(ticket.getTicketId(), ticket);

            // 如果订单中所有车票都已退票，则更新订单状态
            // 简化处理：假设一个订单只有一张票，或者只有所有票都退了才改变订单状态
            boolean allTicketsInOrderRefunded = dataStore.findTicketsByOrderId(order.getOrderId()).stream()
                    .allMatch(t -> t.getTicketStatus().equals(Ticket.Status.REFUNDED.getDescription()) || 
                               t.getTicketStatus().equals("退票处理中"));
            if(allTicketsInOrderRefunded) {
                order.setOrderStatus(Order.Status.CANCELLED.getDescription());
                order.setPaymentStatus("已退款");
                dataStore.orders.put(order.getOrderId(), order);
            }

            return new RefundResponseDTO(ticket.getTicketId(), "退票成功，款项将原路退回。", ticket.getTicketStatus());
        } else {
            // 5. 退款失败，恢复车票状态和座位
            ticket.setTicketStatus(originalStatus);
            dataStore.tickets.put(ticket.getTicketId(), ticket);
            
            // 恢复座位
            if (schedule != null) {
                schedule.decreaseSeatCount(ticket.getSeatType(), 1);
            }
            
            throw new RuntimeException("退款处理失败，请稍后再试。");
        }
    }

    /**
     * 获取用户的车票列表
     * 
     * @param userId 用户ID
     * @return 用户车票列表
     */
    public List<TicketDTO> getUserTickets(String userId) {
        return dataStore.tickets.values().stream()
                .filter(ticket -> ticket.getUserId().equals(userId))
                .filter(ticket -> !ticket.getTicketStatus().equals(Ticket.Status.REFUNDED.getDescription()))
                .map(ticket -> {
                    TrainSchedule schedule = dataStore.trainSchedules.get(ticket.getScheduleId());
                    return TicketDTO.builder()
                            .ticketId(ticket.getTicketId())
                            .scheduleInfo(schedule != null ? schedule.toScheduleInfoDTO() : null)
                            .passengerName(ticket.getPassengerName())
                            .seatNumber(ticket.getSeatNumber())
                            .seatType(ticket.getSeatType().getDescription())
                            .pricePaid(ticket.getPricePaid())
                            .ticketStatus(ticket.getTicketStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 改签车票
     * 
     * @param changeRequest 改签请求
     * @return 改签结果
     */
    public ChangeTicketResponseDTO changeTicket(ChangeTicketRequestDTO changeRequest) {
        // 1. 查找原车票并验证
        Ticket oldTicket = dataStore.tickets.get(changeRequest.getTicketId());
        if (oldTicket == null) {
            throw new IllegalArgumentException("车票信息未找到：" + changeRequest.getTicketId());
        }
        
        if (!oldTicket.getUserId().equals(changeRequest.getUserId())) {
            throw new RuntimeException("不符合改签条件：用户ID不匹配");
        }
        
        if (oldTicket.getTicketStatus().equals(Ticket.Status.REFUNDED.getDescription())) {
            throw new RuntimeException("不符合改签条件：车票已退票");
        }
        
        // 2. 查找新车次信息
        TrainSchedule newSchedule = dataStore.trainSchedules.get(changeRequest.getNewScheduleId());
        if (newSchedule == null) {
            throw new IllegalArgumentException("新车次信息未找到：" + changeRequest.getNewScheduleId());
        }
        
        // 3. 验证新座位类型
        SeatType newSeatType;
        try {
            newSeatType = SeatType.fromDescription(changeRequest.getSeatType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的座位类型：" + changeRequest.getSeatType());
        }
        
        double newPrice = newSchedule.getPriceForSeat(newSeatType);
        if (newPrice == 0.0) {
            throw new IllegalArgumentException("该车次不提供座位类型：" + changeRequest.getSeatType());
        }
        
        // 4. 检查新车次余票
        int availableSeats = newSchedule.getAvailableSeats(newSeatType);
        if (availableSeats <= 0) {
            throw new RuntimeException("所选车次座位余票不足。");
        }

        // 5. 处理差价支付，成功后再调整座位数量
        TrainSchedule oldSchedule = dataStore.trainSchedules.get(oldTicket.getScheduleId());
        double priceDiff = newPrice - oldTicket.getPricePaid();
        boolean paymentSuccessful = true;
        
        if (priceDiff > 0) {
            // 如果新票价高，需要支付差价
            paymentSuccessful = paymentService.processPayment(changeRequest.getUserId(), priceDiff);
            if (!paymentSuccessful) {
                // 支付失败，直接取消改签
                throw new RuntimeException("差价支付失败，改签已取消。");
            }
        } 

        // 支付成功或无需支付后，再执行座位调整
        oldSchedule.increaseSeatCount(oldTicket.getSeatType(), 1);
        newSchedule.decreaseSeatCount(newSeatType, 1);

        // 如果新票价低，退还差价
        if (priceDiff < 0) {
            paymentService.processRefund(oldTicket.getTicketId(), Math.abs(priceDiff));
        }
        
        // 6. 创建新车票
        String newTicketId = dataStore.generateTicketId();
        String seatNumber = assignSeat(newSchedule.getTrainNumber(), newSeatType.getDescription());
        
        Ticket newTicket = Ticket.builder()
                .ticketId(newTicketId)
                .userId(oldTicket.getUserId())
                .orderId(oldTicket.getOrderId())
                .scheduleId(newSchedule.getScheduleId())
                .passengerName(oldTicket.getPassengerName())
                .passengerIdCard(oldTicket.getPassengerIdCard())
                .seatType(newSeatType)
                .seatNumber(seatNumber)
                .pricePaid(newPrice)
                .ticketStatus(Ticket.Status.ISSUED.getDescription())
                .build();
        
        dataStore.tickets.put(newTicketId, newTicket);
        
        // 7. 更新原车票状态
        oldTicket.setTicketStatus(Ticket.Status.CHANGED.getDescription());
        dataStore.tickets.put(oldTicket.getTicketId(), oldTicket);
        
        // 8. 构建并返回改签响应
        TicketDTO newTicketDTO = TicketDTO.builder()
                .ticketId(newTicket.getTicketId())
                .scheduleInfo(newSchedule.toScheduleInfoDTO())
                .passengerName(newTicket.getPassengerName())
                .seatNumber(newTicket.getSeatNumber())
                .seatType(newTicket.getSeatType().getDescription())
                .pricePaid(newTicket.getPricePaid())
                .ticketStatus(newTicket.getTicketStatus())
                .build();
        
        return new ChangeTicketResponseDTO(
                oldTicket.getTicketId(),
                newTicket.getTicketId(),
                "改签成功！" + (priceDiff > 0 ? "已支付差价：" + priceDiff + "元" : 
                               priceDiff < 0 ? "已退还差价：" + Math.abs(priceDiff) + "元" : "无需支付差价"),
                newTicketDTO
        );
    }

    /**
     * 为车票分配座位号
     * 
     * @param trainNumber 列车号
     * @param seatType 座位类型
     * @return 格式化的座位号（如：01车08A座）
     */
    private String assignSeat(String trainNumber, String seatType) {
        int carriage = (int) (Math.random() * 10) + 1;
        int seat = (int) (Math.random() * 50) + 1;
        char row = (char) ('A' + Math.random() * 5);
        return String.format("%02d车%02d%c座", carriage, seat, row);
    }

    /**
     * 将订单和车票数据转换为前端展示的DTO对象
     * 
     * @param order 订单实体
     * @param orderTickets 该订单包含的车票列表
     * @param message 附加信息
     * @return 订单DTO对象
     */
    private OrderDTO convertToOrderDTO(Order order, List<Ticket> orderTickets, String message) {
        List<TicketDTO> ticketDTOs = orderTickets.stream().map(ticket -> {
            TrainSchedule schedule = dataStore.trainSchedules.get(ticket.getScheduleId());
            return TicketDTO.builder()
                    .ticketId(ticket.getTicketId())
                    .scheduleInfo(schedule != null ? schedule.toScheduleInfoDTO() : null)
                    .passengerName(ticket.getPassengerName())
                    .seatNumber(ticket.getSeatNumber())
                    .seatType(ticket.getSeatType().getDescription())
                    .pricePaid(ticket.getPricePaid())
                    .ticketStatus(ticket.getTicketStatus())
                    .build();
        }).collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .tickets(ticketDTOs)
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .message(message)
                .build();
    }

    /**
     * 根据车票ID获取车票DTO
     * 用于在其他地方需要展示特定车票信息时使用
     * 
     * @param ticketId 车票ID
     * @return 车票DTO的Optional包装
     */
    public Optional<TicketDTO> getTicketDTO(String ticketId) {
        Ticket ticket = dataStore.tickets.get(ticketId);
        if (ticket == null) return Optional.empty();

        TrainSchedule schedule = dataStore.trainSchedules.get(ticket.getScheduleId());
        return Optional.of(TicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .scheduleInfo(schedule != null ? schedule.toScheduleInfoDTO() : null)
                .passengerName(ticket.getPassengerName())
                .seatNumber(ticket.getSeatNumber())
                .seatType(ticket.getSeatType().getDescription())
                .pricePaid(ticket.getPricePaid())
                .ticketStatus(ticket.getTicketStatus())
                .build());
    }
}