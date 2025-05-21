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
        
        // 路线 6: 上海 -> 北京 (返程车次)
        Map<SeatType, Integer> availability6 = new ConcurrentHashMap<>();
        availability6.put(SeatType.FIRST_CLASS, 15);
        availability6.put(SeatType.SECOND_CLASS, 45);
        Map<SeatType, Double> prices6 = new ConcurrentHashMap<>();
        prices6.put(SeatType.FIRST_CLASS, 800.0);
        prices6.put(SeatType.SECOND_CLASS, 550.0);
        TrainSchedule schedule6 = TrainSchedule.builder()
                .scheduleId("G1235_20250520")
                .trainNumber("G1235")
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 15, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 21, 0))
                .seatAvailability(availability6)
                .basePrice(prices6)
                .status("正常")
                .build();
        trainSchedules.put(schedule6.getScheduleId(), schedule6);
        
        // 路线 7: 上海 -> 北京 (第二天返程)
        Map<SeatType, Integer> availability7 = new ConcurrentHashMap<>();
        availability7.put(SeatType.FIRST_CLASS, 8);
        availability7.put(SeatType.SECOND_CLASS, 35);
        availability7.put(SeatType.BUSINESS_CLASS, 3);
        Map<SeatType, Double> prices7 = new ConcurrentHashMap<>();
        prices7.put(SeatType.FIRST_CLASS, 820.0);
        prices7.put(SeatType.SECOND_CLASS, 570.0);
        prices7.put(SeatType.BUSINESS_CLASS, 1600.0);
        TrainSchedule schedule7 = TrainSchedule.builder()
                .scheduleId("G1235_20250521")
                .trainNumber("G1235")
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 9, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 14, 30))
                .seatAvailability(availability7)
                .basePrice(prices7)
                .status("正常")
                .build();
        trainSchedules.put(schedule7.getScheduleId(), schedule7);
        
        // 路线 8: 北京 -> 上海 (第二天)
        Map<SeatType, Integer> availability8 = new ConcurrentHashMap<>();
        availability8.put(SeatType.FIRST_CLASS, 5);
        availability8.put(SeatType.SECOND_CLASS, 25);
        Map<SeatType, Double> prices8 = new ConcurrentHashMap<>();
        prices8.put(SeatType.FIRST_CLASS, 800.0);
        prices8.put(SeatType.SECOND_CLASS, 550.0);
        TrainSchedule schedule8 = TrainSchedule.builder()
                .scheduleId("G1234_20250521")
                .trainNumber("G1234")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 9, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 14, 30))
                .seatAvailability(availability8)
                .basePrice(prices8)
                .status("正常")
                .build();
        trainSchedules.put(schedule8.getScheduleId(), schedule8);
        
        // 路线 9: 广州 -> 武汉 (返程)
        Map<SeatType, Integer> availability9 = new ConcurrentHashMap<>();
        availability9.put(SeatType.SECOND_CLASS, 3); // 余票很少
        availability9.put(SeatType.BUSINESS_CLASS, 1); // 余票很少
        Map<SeatType, Double> prices9 = new ConcurrentHashMap<>();
        prices9.put(SeatType.SECOND_CLASS, 480.0);
        prices9.put(SeatType.BUSINESS_CLASS, 1500.0);
        TrainSchedule schedule9 = TrainSchedule.builder()
                .scheduleId("G1002_20250520")
                .trainNumber("G1002")
                .departureStation("广州南")
                .arrivalStation("武汉")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 16, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 20, 0))
                .seatAvailability(availability9)
                .basePrice(prices9)
                .status("正常")
                .build();
        trainSchedules.put(schedule9.getScheduleId(), schedule9);
        
        // 路线 10: 杭州 -> 上海 (第二天返程)
        Map<SeatType, Integer> availability10 = new ConcurrentHashMap<>();
        availability10.put(SeatType.SECOND_CLASS, 90);
        availability10.put(SeatType.FIRST_CLASS, 15);
        Map<SeatType, Double> prices10 = new ConcurrentHashMap<>();
        prices10.put(SeatType.SECOND_CLASS, 73.0);
        prices10.put(SeatType.FIRST_CLASS, 117.5);
        TrainSchedule schedule10 = TrainSchedule.builder()
                .scheduleId("G7502_20250521")
                .trainNumber("G7502")
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 10, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 10, 50))
                .seatAvailability(availability10)
                .basePrice(prices10)
                .status("正常")
                .build();
        trainSchedules.put(schedule10.getScheduleId(), schedule10);
        
        // 路线 11: 西安 -> 北京 (第二天返程)
        Map<SeatType, Integer> availability11 = new ConcurrentHashMap<>();
        availability11.put(SeatType.SECOND_CLASS, 40);
        availability11.put(SeatType.BUSINESS_CLASS, 0); // 无票
        Map<SeatType, Double> prices11 = new ConcurrentHashMap<>();
        prices11.put(SeatType.SECOND_CLASS, 515.5);
        prices11.put(SeatType.BUSINESS_CLASS, 1627.5);
        TrainSchedule schedule11 = TrainSchedule.builder()
                .scheduleId("G88_20250521")
                .trainNumber("G88")
                .departureStation("西安北")
                .arrivalStation("北京西")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 18, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 23, 0))
                .seatAvailability(availability11)
                .basePrice(prices11)
                .status("晚点") // 晚点状态
                .build();
        trainSchedules.put(schedule11.getScheduleId(), schedule11);
        
        // 路线 12: 成都 -> 北京 (直达，第二天)
        Map<SeatType, Integer> availability12 = new ConcurrentHashMap<>();
        availability12.put(SeatType.FIRST_CLASS, 12);
        availability12.put(SeatType.SECOND_CLASS, 65);
        availability12.put(SeatType.BUSINESS_CLASS, 4);
        Map<SeatType, Double> prices12 = new ConcurrentHashMap<>();
        prices12.put(SeatType.FIRST_CLASS, 1320.0);
        prices12.put(SeatType.SECOND_CLASS, 980.0);
        prices12.put(SeatType.BUSINESS_CLASS, 2100.0);
        TrainSchedule schedule12 = TrainSchedule.builder()
                .scheduleId("G8998_20250521")
                .trainNumber("G8998")
                .departureStation("成都东")
                .arrivalStation("北京西")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 7, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 16, 45))
                .seatAvailability(availability12)
                .basePrice(prices12)
                .status("正常")
                .build();
        trainSchedules.put(schedule12.getScheduleId(), schedule12);
        
        // 路线 13: 北京 -> 重庆 (第二天)
        Map<SeatType, Integer> availability13 = new ConcurrentHashMap<>();
        availability13.put(SeatType.FIRST_CLASS, 16);
        availability13.put(SeatType.SECOND_CLASS, 70);
        Map<SeatType, Double> prices13 = new ConcurrentHashMap<>();
        prices13.put(SeatType.FIRST_CLASS, 1150.0);
        prices13.put(SeatType.SECOND_CLASS, 850.0);
        TrainSchedule schedule13 = TrainSchedule.builder()
                .scheduleId("G571_20250521")
                .trainNumber("G571")
                .departureStation("北京西")
                .arrivalStation("重庆北")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 8, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 16, 30))
                .seatAvailability(availability13)
                .basePrice(prices13)
                .status("正常")
                .build();
        trainSchedules.put(schedule13.getScheduleId(), schedule13);
        
        // 路线 14: 广州 -> 杭州 (第二天)
        Map<SeatType, Integer> availability14 = new ConcurrentHashMap<>();
        availability14.put(SeatType.FIRST_CLASS, 7);
        availability14.put(SeatType.SECOND_CLASS, 33);
        Map<SeatType, Double> prices14 = new ConcurrentHashMap<>();
        prices14.put(SeatType.FIRST_CLASS, 980.0);
        prices14.put(SeatType.SECOND_CLASS, 640.0);
        TrainSchedule schedule14 = TrainSchedule.builder()
                .scheduleId("G2345_20250521")
                .trainNumber("G2345")
                .departureStation("广州南")
                .arrivalStation("杭州东")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 10, 15))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 18, 20))
                .seatAvailability(availability14)
                .basePrice(prices14)
                .status("正常")
                .build();
        trainSchedules.put(schedule14.getScheduleId(), schedule14);
        
        // 路线 15: 重庆 -> 成都 (当天返程)
        Map<SeatType, Integer> availability15 = new ConcurrentHashMap<>();
        availability15.put(SeatType.SECOND_CLASS, 50);
        availability15.put(SeatType.FIRST_CLASS, 10);
        Map<SeatType, Double> prices15 = new ConcurrentHashMap<>();
        prices15.put(SeatType.SECOND_CLASS, 154.0);
        prices15.put(SeatType.FIRST_CLASS, 246.0);
        TrainSchedule schedule15 = TrainSchedule.builder()
                .scheduleId("C6003_20250520")
                .trainNumber("C6003")
                .departureStation("重庆北")
                .arrivalStation("成都东")
                .departureDateTime(LocalDateTime.of(2025, 5, 20, 15, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 20, 16, 30))
                .seatAvailability(availability15)
                .basePrice(prices15)
                .status("正常")
                .build();
        trainSchedules.put(schedule15.getScheduleId(), schedule15);
        
        // 路线 16: 北京 -> 上海 (第三天)
        Map<SeatType, Integer> availability16 = new ConcurrentHashMap<>();
        availability16.put(SeatType.FIRST_CLASS, 20);
        availability16.put(SeatType.SECOND_CLASS, 60);
        availability16.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices16 = new ConcurrentHashMap<>();
        prices16.put(SeatType.FIRST_CLASS, 800.0);
        prices16.put(SeatType.SECOND_CLASS, 550.0);
        prices16.put(SeatType.BUSINESS_CLASS, 1600.0);
        TrainSchedule schedule16 = TrainSchedule.builder()
                .scheduleId("G1234_20250522")
                .trainNumber("G1234")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 22, 9, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 22, 14, 30))
                .seatAvailability(availability16)
                .basePrice(prices16)
                .status("正常")
                .build();
        trainSchedules.put(schedule16.getScheduleId(), schedule16);
        
        // 路线 17: 上海 -> 北京 (第三天)
        Map<SeatType, Integer> availability17 = new ConcurrentHashMap<>();
        availability17.put(SeatType.FIRST_CLASS, 18);
        availability17.put(SeatType.SECOND_CLASS, 55);
        Map<SeatType, Double> prices17 = new ConcurrentHashMap<>();
        prices17.put(SeatType.FIRST_CLASS, 800.0);
        prices17.put(SeatType.SECOND_CLASS, 550.0);
        TrainSchedule schedule17 = TrainSchedule.builder()
                .scheduleId("G1235_20250522")
                .trainNumber("G1235")
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 22, 9, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 22, 14, 30))
                .seatAvailability(availability17)
                .basePrice(prices17)
                .status("正常")
                .build();
        trainSchedules.put(schedule17.getScheduleId(), schedule17);
        
        // 路线 18: 北京 -> 上海 (第二天，下午班次)
        Map<SeatType, Integer> availability18 = new ConcurrentHashMap<>();
        availability18.put(SeatType.FIRST_CLASS, 10);
        availability18.put(SeatType.SECOND_CLASS, 40);
        Map<SeatType, Double> prices18 = new ConcurrentHashMap<>();
        prices18.put(SeatType.FIRST_CLASS, 780.0); // 下午班次价格略低
        prices18.put(SeatType.SECOND_CLASS, 530.0);
        TrainSchedule schedule18 = TrainSchedule.builder()
                .scheduleId("G1236_20250521")
                .trainNumber("G1236")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 14, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 19, 30))
                .seatAvailability(availability18)
                .basePrice(prices18)
                .status("正常")
                .build();
        trainSchedules.put(schedule18.getScheduleId(), schedule18);
        
        // 路线 19: 杭州 -> 成都 (第二天，长途直达)
        Map<SeatType, Integer> availability19 = new ConcurrentHashMap<>();
        availability19.put(SeatType.FIRST_CLASS, 14);
        availability19.put(SeatType.SECOND_CLASS, 48);
        availability19.put(SeatType.BUSINESS_CLASS, 6);
        Map<SeatType, Double> prices19 = new ConcurrentHashMap<>();
        prices19.put(SeatType.FIRST_CLASS, 1350.0);
        prices19.put(SeatType.SECOND_CLASS, 950.0);
        prices19.put(SeatType.BUSINESS_CLASS, 2200.0);
        TrainSchedule schedule19 = TrainSchedule.builder()
                .scheduleId("G9876_20250521")
                .trainNumber("G9876")
                .departureStation("杭州东")
                .arrivalStation("成都东")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 8, 15))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 19, 45))
                .seatAvailability(availability19)
                .basePrice(prices19)
                .status("正常")
                .build();
        trainSchedules.put(schedule19.getScheduleId(), schedule19);
        
        // 路线 20: 广州 -> 北京 (第二天，长途直达)
        Map<SeatType, Integer> availability20 = new ConcurrentHashMap<>();
        availability20.put(SeatType.FIRST_CLASS, 9);
        availability20.put(SeatType.SECOND_CLASS, 38);
        availability20.put(SeatType.BUSINESS_CLASS, 2);
        Map<SeatType, Double> prices20 = new ConcurrentHashMap<>();
        prices20.put(SeatType.FIRST_CLASS, 1480.0);
        prices20.put(SeatType.SECOND_CLASS, 1080.0);
        prices20.put(SeatType.BUSINESS_CLASS, 2500.0);
        TrainSchedule schedule20 = TrainSchedule.builder()
                .scheduleId("G1010_20250521")
                .trainNumber("G1010")
                .departureStation("广州南")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 7, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 19, 0))
                .seatAvailability(availability20)
                .basePrice(prices20)
                .status("正常")
                .build();
        trainSchedules.put(schedule20.getScheduleId(), schedule20);
        
        // 以下是适合改签演示的列车班次 - 相同路线不同时间
        
        // 路线 21: 北京 -> 上海 (当天晚班，适合G1234改签)
        Map<SeatType, Integer> availability21 = new ConcurrentHashMap<>();
        availability21.put(SeatType.FIRST_CLASS, 25);
        availability21.put(SeatType.SECOND_CLASS, 75);
        Map<SeatType, Double> prices21 = new ConcurrentHashMap<>();
        prices21.put(SeatType.FIRST_CLASS, 790.0);  // 晚班价格稍有不同
        prices21.put(SeatType.SECOND_CLASS, 540.0);
        TrainSchedule schedule21 = TrainSchedule.builder()
                .scheduleId("G1238_20250521")
                .trainNumber("G1238")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 18, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 23, 30))
                .seatAvailability(availability21)
                .basePrice(prices21)
                .status("正常")
                .build();
        trainSchedules.put(schedule21.getScheduleId(), schedule21);
        
        // 路线 22: 北京 -> 上海 (第二天早班，适合G1234改签)
        Map<SeatType, Integer> availability22 = new ConcurrentHashMap<>();
        availability22.put(SeatType.FIRST_CLASS, 30);
        availability22.put(SeatType.SECOND_CLASS, 80);
        Map<SeatType, Double> prices22 = new ConcurrentHashMap<>();
        prices22.put(SeatType.FIRST_CLASS, 820.0);  // 改签价格稍高
        prices22.put(SeatType.SECOND_CLASS, 570.0);
        TrainSchedule schedule22 = TrainSchedule.builder()
                .scheduleId("G1234_20250522_early")
                .trainNumber("G1234")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 22, 6, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 22, 12, 0))
                .seatAvailability(availability22)
                .basePrice(prices22)
                .status("正常")
                .build();
        trainSchedules.put(schedule22.getScheduleId(), schedule22);
        
        // 路线 23: 上海 -> 北京 (当天不同时段，适合G1235改签)
        Map<SeatType, Integer> availability23 = new ConcurrentHashMap<>();
        availability23.put(SeatType.FIRST_CLASS, 12);
        availability23.put(SeatType.SECOND_CLASS, 45);
        Map<SeatType, Double> prices23 = new ConcurrentHashMap<>();
        prices23.put(SeatType.FIRST_CLASS, 810.0);
        prices23.put(SeatType.SECOND_CLASS, 560.0);
        TrainSchedule schedule23 = TrainSchedule.builder()
                .scheduleId("G1237_20250521")
                .trainNumber("G1237")
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 12, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 18, 0))
                .seatAvailability(availability23)
                .basePrice(prices23)
                .status("正常")
                .build();
        trainSchedules.put(schedule23.getScheduleId(), schedule23);
        
        // 路线 24: 上海 -> 北京 (当天晚班，适合G1235改签)
        Map<SeatType, Integer> availability24 = new ConcurrentHashMap<>();
        availability24.put(SeatType.FIRST_CLASS, 20);
        availability24.put(SeatType.SECOND_CLASS, 60);
        availability24.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices24 = new ConcurrentHashMap<>();
        prices24.put(SeatType.FIRST_CLASS, 795.0);  // 晚班价格稍有不同
        prices24.put(SeatType.SECOND_CLASS, 545.0);
        prices24.put(SeatType.BUSINESS_CLASS, 1580.0);
        TrainSchedule schedule24 = TrainSchedule.builder()
                .scheduleId("G1239_20250521")
                .trainNumber("G1239")
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 16, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 21, 30))
                .seatAvailability(availability24)
                .basePrice(prices24)
                .status("正常")
                .build();
        trainSchedules.put(schedule24.getScheduleId(), schedule24);
        
        // 路线 25: 成都 -> 重庆 (当天下午，适合C6002改签)
        Map<SeatType, Integer> availability25 = new ConcurrentHashMap<>();
        availability25.put(SeatType.SECOND_CLASS, 65);
        availability25.put(SeatType.FIRST_CLASS, 12);
        Map<SeatType, Double> prices25 = new ConcurrentHashMap<>();
        prices25.put(SeatType.SECOND_CLASS, 154.0);
        prices25.put(SeatType.FIRST_CLASS, 246.0);
        TrainSchedule schedule25 = TrainSchedule.builder()
                .scheduleId("C6004_20250521")
                .trainNumber("C6004")
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 14, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 15, 30))
                .seatAvailability(availability25)
                .basePrice(prices25)
                .status("正常")
                .build();
        trainSchedules.put(schedule25.getScheduleId(), schedule25);
        
        // 路线 26: 成都 -> 重庆 (当天晚班，适合C6002改签)
        Map<SeatType, Integer> availability26 = new ConcurrentHashMap<>();
        availability26.put(SeatType.SECOND_CLASS, 70);
        availability26.put(SeatType.FIRST_CLASS, 18);
        Map<SeatType, Double> prices26 = new ConcurrentHashMap<>();
        prices26.put(SeatType.SECOND_CLASS, 154.0);
        prices26.put(SeatType.FIRST_CLASS, 246.0);
        TrainSchedule schedule26 = TrainSchedule.builder()
                .scheduleId("C6006_20250521")
                .trainNumber("C6006")
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 18, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 19, 30))
                .seatAvailability(availability26)
                .basePrice(prices26)
                .status("正常")
                .build();
        trainSchedules.put(schedule26.getScheduleId(), schedule26);
        
        // 路线 27: 武汉 -> 广州 (当天，不同时段，适合G1001改签)
        Map<SeatType, Integer> availability27 = new ConcurrentHashMap<>();
        availability27.put(SeatType.SECOND_CLASS, 35);
        availability27.put(SeatType.BUSINESS_CLASS, 8);
        Map<SeatType, Double> prices27 = new ConcurrentHashMap<>();
        prices27.put(SeatType.SECOND_CLASS, 490.0);  // 价格稍有调整
        prices27.put(SeatType.BUSINESS_CLASS, 1520.0);
        TrainSchedule schedule27 = TrainSchedule.builder()
                .scheduleId("G1001_20250521")
                .trainNumber("G1001")
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 10, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 14, 0))
                .seatAvailability(availability27)
                .basePrice(prices27)
                .status("正常")
                .build();
        trainSchedules.put(schedule27.getScheduleId(), schedule27);
        
        // 路线 28: 武汉 -> 广州 (当天晚班，适合G1001改签)
        Map<SeatType, Integer> availability28 = new ConcurrentHashMap<>();
        availability28.put(SeatType.SECOND_CLASS, 40);
        availability28.put(SeatType.BUSINESS_CLASS, 10);
        Map<SeatType, Double> prices28 = new ConcurrentHashMap<>();
        prices28.put(SeatType.SECOND_CLASS, 475.0);  // 晚班价格更低
        prices28.put(SeatType.BUSINESS_CLASS, 1490.0);
        TrainSchedule schedule28 = TrainSchedule.builder()
                .scheduleId("G1003_20250521")
                .trainNumber("G1003")
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 16, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 20, 30))
                .seatAvailability(availability28)
                .basePrice(prices28)
                .status("正常")
                .build();
        trainSchedules.put(schedule28.getScheduleId(), schedule28);
        
        // 路线 29: 杭州 -> 上海 (当天，更多时间选择，适合G7502改签)
        Map<SeatType, Integer> availability29 = new ConcurrentHashMap<>();
        availability29.put(SeatType.SECOND_CLASS, 85);
        availability29.put(SeatType.FIRST_CLASS, 20);
        Map<SeatType, Double> prices29 = new ConcurrentHashMap<>();
        prices29.put(SeatType.SECOND_CLASS, 73.0);
        prices29.put(SeatType.FIRST_CLASS, 117.5);
        TrainSchedule schedule29 = TrainSchedule.builder()
                .scheduleId("G7504_20250521")
                .trainNumber("G7504")
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 13, 0))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 13, 50))
                .seatAvailability(availability29)
                .basePrice(prices29)
                .status("正常")
                .build();
        trainSchedules.put(schedule29.getScheduleId(), schedule29);
        
        // 路线 30: 杭州 -> 上海 (当天，晚班，适合G7502改签)
        Map<SeatType, Integer> availability30 = new ConcurrentHashMap<>();
        availability30.put(SeatType.SECOND_CLASS, 75);
        availability30.put(SeatType.FIRST_CLASS, 25);
        Map<SeatType, Double> prices30 = new ConcurrentHashMap<>();
        prices30.put(SeatType.SECOND_CLASS, 73.0);
        prices30.put(SeatType.FIRST_CLASS, 117.5);
        TrainSchedule schedule30 = TrainSchedule.builder()
                .scheduleId("G7506_20250521")
                .trainNumber("G7506")
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.of(2025, 5, 21, 18, 30))
                .arrivalDateTime(LocalDateTime.of(2025, 5, 21, 19, 20))
                .seatAvailability(availability30)
                .basePrice(prices30)
                .status("正常")
                .build();
        trainSchedules.put(schedule30.getScheduleId(), schedule30);
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