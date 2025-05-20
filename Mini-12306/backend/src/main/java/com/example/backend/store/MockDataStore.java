package com.example.backend.store;

import com.example.backend.model.Order;
import com.example.backend.model.SeatType;
import com.example.backend.model.Ticket;
import com.example.backend.model.TrainSchedule;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class MockDataStore {
    public final Map<String, TrainSchedule> trainSchedules = new ConcurrentHashMap<>();
    public final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    public final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong ticketIdCounter = new AtomicLong(0);
    private final AtomicLong orderIdCounter = new AtomicLong(0);

    public MockDataStore() {
        // 路线 1: 北京 -> 上海 (已有)
        Map<SeatType, Integer> availability1 = new ConcurrentHashMap<>();
        availability1.put(SeatType.FIRST_CLASS, 10);
        availability1.put(SeatType.SECOND_CLASS, 50);
        Map<SeatType, Double> prices1 = new ConcurrentHashMap<>();
        prices1.put(SeatType.FIRST_CLASS, 800.0);
        prices1.put(SeatType.SECOND_CLASS, 550.0);
        TrainSchedule schedule1 = TrainSchedule.builder()
                .scheduleId("G1234_20250520")
                .trainNumber("G1234")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 9, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 14, 30))
                .seatAvailability(availability1)
                .basePrice(prices1)
                .status("正常")
                .build();
        trainSchedules.put(schedule1.getScheduleId(), schedule1);

        // 路线 2: 武汉 -> 广州 (已有，修改一下日期和余票)
        Map<SeatType, Integer> availability2 = new ConcurrentHashMap<>();
        availability2.put(SeatType.SECOND_CLASS, 25);
        availability2.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices2 = new ConcurrentHashMap<>();
        prices2.put(SeatType.SECOND_CLASS, 480.0);
        prices2.put(SeatType.BUSINESS_CLASS, 1500.0);
        TrainSchedule schedule2 = TrainSchedule.builder()
                .scheduleId("G1001_20250520") // 修改了车次和日期，使其与schedule1同天
                .trainNumber("G1001")
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 10, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 14, 0))
                .seatAvailability(availability2)
                .basePrice(prices2)
                .status("正常")
                .build();
        trainSchedules.put(schedule2.getScheduleId(), schedule2);

        // 路线 3: 上海 -> 杭州 (新增)
        Map<SeatType, Integer> availability3 = new ConcurrentHashMap<>();
        availability3.put(SeatType.SECOND_CLASS, 100);
        availability3.put(SeatType.FIRST_CLASS, 20);
        Map<SeatType, Double> prices3 = new ConcurrentHashMap<>();
        prices3.put(SeatType.SECOND_CLASS, 73.0);
        prices3.put(SeatType.FIRST_CLASS, 117.5);
        TrainSchedule schedule3 = TrainSchedule.builder()
                .scheduleId("G7501_20250521")
                .trainNumber("G7501")
                .departureStation("上海虹桥")
                .arrivalStation("杭州东")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 8, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 9, 20))
                .seatAvailability(availability3)
                .basePrice(prices3)
                .status("正常")
                .build();
        trainSchedules.put(schedule3.getScheduleId(), schedule3);

        // 路线 4: 成都 -> 重庆 (新增)
        Map<SeatType, Integer> availability4 = new ConcurrentHashMap<>();
        availability4.put(SeatType.SECOND_CLASS, 80);
        availability4.put(SeatType.FIRST_CLASS, 15);
        Map<SeatType, Double> prices4 = new ConcurrentHashMap<>();
        prices4.put(SeatType.SECOND_CLASS, 154.0);
        prices4.put(SeatType.FIRST_CLASS, 246.0);
        TrainSchedule schedule4 = TrainSchedule.builder()
                .scheduleId("C6002_20250520") // 与schedule1, schedule2同天
                .trainNumber("C6002")
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 11, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 12, 30))
                .seatAvailability(availability4)
                .basePrice(prices4)
                .status("晚点") // 测试不同状态
                .build();
        trainSchedules.put(schedule4.getScheduleId(), schedule4);

        // 路线 5: 北京 -> 西安 (新增)
        Map<SeatType, Integer> availability5 = new ConcurrentHashMap<>();
        availability5.put(SeatType.SECOND_CLASS, 60);
        availability5.put(SeatType.BUSINESS_CLASS, 8);
        Map<SeatType, Double> prices5 = new ConcurrentHashMap<>();
        prices5.put(SeatType.SECOND_CLASS, 515.5);
        prices5.put(SeatType.BUSINESS_CLASS, 1627.5);
        TrainSchedule schedule5 = TrainSchedule.builder()
                .scheduleId("G87_20250521")
                .trainNumber("G87")
                .departureStation("北京西")
                .arrivalStation("西安北")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 13, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 17, 30))
                .seatAvailability(availability5)
                .basePrice(prices5)
                .status("正常")
                .build();
        trainSchedules.put(schedule5.getScheduleId(), schedule5);
    }

    public String generateTicketId() {
        return "TKT" + String.format("%03d", ticketIdCounter.incrementAndGet());
    }

    public String generateOrderId() {
        return "ORD" + String.format("%03d", orderIdCounter.incrementAndGet());
    }

    public List<Ticket> findTicketsByOrderId(String orderId) {
        return tickets.values().stream()
                .filter(ticket -> orderId.equals(ticket.getOrderId()))
                .collect(Collectors.toList());
    }
}