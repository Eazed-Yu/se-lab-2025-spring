package com.example.backend.store;

import com.example.backend.model.Order;
import com.example.backend.model.SeatType;
import com.example.backend.model.Ticket;
import com.example.backend.model.TrainSchedule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final LocalDate today = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取相对于今天的日期时间
     * @param dayOffset 相对于今天的天数偏移量
     * @param hour 小时
     * @param minute 分钟
     * @return LocalDateTime对象
     */
    private LocalDateTime getRelativeDateTime(int dayOffset, int hour, int minute) {
        return LocalDate.now().plusDays(dayOffset).atTime(hour, minute);
    }
    
    /**
     * 生成列车日期ID，格式为：trainNumber_yyyyMMdd
     * @param trainNumber 车次号
     * @param dayOffset 相对于今天的天数偏移量
     * @return 格式化的列车日期ID
     */
    private String generateScheduleId(String trainNumber, int dayOffset) {
        String dateString = today.plusDays(dayOffset).format(dateFormatter);
        return trainNumber + "_" + dateString;
    }

    public MockDataStore() {
        // 路线 1: 北京 -> 上海 (今天)
        Map<SeatType, Integer> availability1 = new ConcurrentHashMap<>();
        availability1.put(SeatType.FIRST_CLASS, 10);
        availability1.put(SeatType.SECOND_CLASS, 50);
        Map<SeatType, Double> prices1 = new ConcurrentHashMap<>();
        prices1.put(SeatType.FIRST_CLASS, 800.0);
        prices1.put(SeatType.SECOND_CLASS, 550.0);
        String trainNumber1 = "G1234";
        String scheduleId1 = generateScheduleId(trainNumber1, 0); // 今天
        TrainSchedule schedule1 = TrainSchedule.builder()
                .scheduleId(scheduleId1)
                .trainNumber(trainNumber1)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(0, 9, 0))
                .arrivalDateTime(getRelativeDateTime(0, 14, 30))
                .seatAvailability(availability1)
                .basePrice(prices1)
                .status("正常")
                .build();
        trainSchedules.put(schedule1.getScheduleId(), schedule1);

        // 路线 2: 武汉 -> 广州 (今天)
        Map<SeatType, Integer> availability2 = new ConcurrentHashMap<>();
        availability2.put(SeatType.SECOND_CLASS, 25);
        availability2.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices2 = new ConcurrentHashMap<>();
        prices2.put(SeatType.SECOND_CLASS, 480.0);
        prices2.put(SeatType.BUSINESS_CLASS, 1500.0);
        String trainNumber2 = "G1001";
        String scheduleId2 = generateScheduleId(trainNumber2, 0); // 今天
        TrainSchedule schedule2 = TrainSchedule.builder()
                .scheduleId(scheduleId2)
                .trainNumber(trainNumber2)
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(getRelativeDateTime(0, 10, 0))
                .arrivalDateTime(getRelativeDateTime(0, 14, 0))
                .seatAvailability(availability2)
                .basePrice(prices2)
                .status("正常")
                .build();
        trainSchedules.put(schedule2.getScheduleId(), schedule2);

        // 路线 3: 上海 -> 杭州 (明天)
        Map<SeatType, Integer> availability3 = new ConcurrentHashMap<>();
        availability3.put(SeatType.SECOND_CLASS, 100);
        availability3.put(SeatType.FIRST_CLASS, 20);
        Map<SeatType, Double> prices3 = new ConcurrentHashMap<>();
        prices3.put(SeatType.SECOND_CLASS, 73.0);
        prices3.put(SeatType.FIRST_CLASS, 117.5);
        String trainNumber3 = "G7501";
        String scheduleId3 = generateScheduleId(trainNumber3, 1); // 明天
        TrainSchedule schedule3 = TrainSchedule.builder()
                .scheduleId(scheduleId3)
                .trainNumber(trainNumber3)
                .departureStation("上海虹桥")
                .arrivalStation("杭州东")
                .departureDateTime(getRelativeDateTime(1, 8, 30))
                .arrivalDateTime(getRelativeDateTime(1, 9, 20))
                .seatAvailability(availability3)
                .basePrice(prices3)
                .status("正常")
                .build();
        trainSchedules.put(schedule3.getScheduleId(), schedule3);

        // 路线 4: 成都 -> 重庆 (今天)
        Map<SeatType, Integer> availability4 = new ConcurrentHashMap<>();
        availability4.put(SeatType.SECOND_CLASS, 80);
        availability4.put(SeatType.FIRST_CLASS, 15);
        Map<SeatType, Double> prices4 = new ConcurrentHashMap<>();
        prices4.put(SeatType.SECOND_CLASS, 154.0);
        prices4.put(SeatType.FIRST_CLASS, 246.0);
        String trainNumber4 = "C6002";
        String scheduleId4 = generateScheduleId(trainNumber4, 0); // 今天
        TrainSchedule schedule4 = TrainSchedule.builder()
                .scheduleId(scheduleId4)
                .trainNumber(trainNumber4)
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(getRelativeDateTime(0, 11, 0))
                .arrivalDateTime(getRelativeDateTime(0, 12, 30))
                .seatAvailability(availability4)
                .basePrice(prices4)
                .status("晚点") // 测试不同状态
                .build();
        trainSchedules.put(schedule4.getScheduleId(), schedule4);

        // 路线 5: 北京 -> 西安 (明天)
        Map<SeatType, Integer> availability5 = new ConcurrentHashMap<>();
        availability5.put(SeatType.SECOND_CLASS, 60);
        availability5.put(SeatType.BUSINESS_CLASS, 8);
        Map<SeatType, Double> prices5 = new ConcurrentHashMap<>();
        prices5.put(SeatType.SECOND_CLASS, 515.5);
        prices5.put(SeatType.BUSINESS_CLASS, 1627.5);
        String trainNumber5 = "G87";
        String scheduleId5 = generateScheduleId(trainNumber5, 1); // 明天
        TrainSchedule schedule5 = TrainSchedule.builder()
                .scheduleId(scheduleId5)
                .trainNumber(trainNumber5)
                .departureStation("北京西")
                .arrivalStation("西安北")
                .departureDateTime(getRelativeDateTime(1, 13, 0))
                .arrivalDateTime(getRelativeDateTime(1, 17, 30))
                .seatAvailability(availability5)
                .basePrice(prices5)
                .status("正常")
                .build();
        trainSchedules.put(schedule5.getScheduleId(), schedule5);
        
        // 路线 6: 上海 -> 北京 (今天返程车次)
        Map<SeatType, Integer> availability6 = new ConcurrentHashMap<>();
        availability6.put(SeatType.FIRST_CLASS, 15);
        availability6.put(SeatType.SECOND_CLASS, 45);
        Map<SeatType, Double> prices6 = new ConcurrentHashMap<>();
        prices6.put(SeatType.FIRST_CLASS, 800.0);
        prices6.put(SeatType.SECOND_CLASS, 550.0);
        String trainNumber6 = "G1235";
        String scheduleId6 = generateScheduleId(trainNumber6, 0); // 今天
        TrainSchedule schedule6 = TrainSchedule.builder()
                .scheduleId(scheduleId6)
                .trainNumber(trainNumber6)
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(0, 15, 30))
                .arrivalDateTime(getRelativeDateTime(0, 21, 0))
                .seatAvailability(availability6)
                .basePrice(prices6)
                .status("正常")
                .build();
        trainSchedules.put(schedule6.getScheduleId(), schedule6);
        
        // 路线 7: 上海 -> 北京 (明天返程)
        Map<SeatType, Integer> availability7 = new ConcurrentHashMap<>();
        availability7.put(SeatType.FIRST_CLASS, 8);
        availability7.put(SeatType.SECOND_CLASS, 35);
        availability7.put(SeatType.BUSINESS_CLASS, 3);
        Map<SeatType, Double> prices7 = new ConcurrentHashMap<>();
        prices7.put(SeatType.FIRST_CLASS, 820.0);
        prices7.put(SeatType.SECOND_CLASS, 570.0);
        prices7.put(SeatType.BUSINESS_CLASS, 1600.0);
        String trainNumber7 = "G1235";
        String scheduleId7 = generateScheduleId(trainNumber7, 1); // 明天
        TrainSchedule schedule7 = TrainSchedule.builder()
                .scheduleId(scheduleId7)
                .trainNumber(trainNumber7)
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(1, 9, 0))
                .arrivalDateTime(getRelativeDateTime(1, 14, 30))
                .seatAvailability(availability7)
                .basePrice(prices7)
                .status("正常")
                .build();
        trainSchedules.put(schedule7.getScheduleId(), schedule7);
        
        // 路线 8: 北京 -> 上海 (明天)
        Map<SeatType, Integer> availability8 = new ConcurrentHashMap<>();
        availability8.put(SeatType.FIRST_CLASS, 5);
        availability8.put(SeatType.SECOND_CLASS, 25);
        Map<SeatType, Double> prices8 = new ConcurrentHashMap<>();
        prices8.put(SeatType.FIRST_CLASS, 800.0);
        prices8.put(SeatType.SECOND_CLASS, 550.0);
        String trainNumber8 = "G1234";
        String scheduleId8 = generateScheduleId(trainNumber8, 1); // 明天
        TrainSchedule schedule8 = TrainSchedule.builder()
                .scheduleId(scheduleId8)
                .trainNumber(trainNumber8)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 9, 0))
                .arrivalDateTime(getRelativeDateTime(1, 14, 30))
                .seatAvailability(availability8)
                .basePrice(prices8)
                .status("正常")
                .build();
        trainSchedules.put(schedule8.getScheduleId(), schedule8);
        
        // 路线 9: 广州 -> 武汉 (今天返程)
        Map<SeatType, Integer> availability9 = new ConcurrentHashMap<>();
        availability9.put(SeatType.SECOND_CLASS, 3); // 余票很少
        availability9.put(SeatType.BUSINESS_CLASS, 1); // 余票很少
        Map<SeatType, Double> prices9 = new ConcurrentHashMap<>();
        prices9.put(SeatType.SECOND_CLASS, 480.0);
        prices9.put(SeatType.BUSINESS_CLASS, 1500.0);
        String trainNumber9 = "G1002";
        String scheduleId9 = generateScheduleId(trainNumber9, 0); // 今天
        TrainSchedule schedule9 = TrainSchedule.builder()
                .scheduleId(scheduleId9)
                .trainNumber(trainNumber9)
                .departureStation("广州南")
                .arrivalStation("武汉")
                .departureDateTime(getRelativeDateTime(0, 16, 0))
                .arrivalDateTime(getRelativeDateTime(0, 20, 0))
                .seatAvailability(availability9)
                .basePrice(prices9)
                .status("正常")
                .build();
        trainSchedules.put(schedule9.getScheduleId(), schedule9);
        
        // 路线 10: 杭州 -> 上海 (明天返程)
        Map<SeatType, Integer> availability10 = new ConcurrentHashMap<>();
        availability10.put(SeatType.SECOND_CLASS, 90);
        availability10.put(SeatType.FIRST_CLASS, 15);
        Map<SeatType, Double> prices10 = new ConcurrentHashMap<>();
        prices10.put(SeatType.SECOND_CLASS, 73.0);
        prices10.put(SeatType.FIRST_CLASS, 117.5);
        String trainNumber10 = "G7502";
        String scheduleId10 = generateScheduleId(trainNumber10, 1); // 明天
        TrainSchedule schedule10 = TrainSchedule.builder()
                .scheduleId(scheduleId10)
                .trainNumber(trainNumber10)
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 10, 0))
                .arrivalDateTime(getRelativeDateTime(1, 10, 50))
                .seatAvailability(availability10)
                .basePrice(prices10)
                .status("正常")
                .build();
        trainSchedules.put(schedule10.getScheduleId(), schedule10);
        
        // 路线 11: 西安 -> 北京 (明天返程)
        Map<SeatType, Integer> availability11 = new ConcurrentHashMap<>();
        availability11.put(SeatType.SECOND_CLASS, 40);
        availability11.put(SeatType.BUSINESS_CLASS, 0); // 无票
        Map<SeatType, Double> prices11 = new ConcurrentHashMap<>();
        prices11.put(SeatType.SECOND_CLASS, 515.5);
        prices11.put(SeatType.BUSINESS_CLASS, 1627.5);
        String trainNumber11 = "G88";
        String scheduleId11 = generateScheduleId(trainNumber11, 1); // 明天
        TrainSchedule schedule11 = TrainSchedule.builder()
                .scheduleId(scheduleId11)
                .trainNumber(trainNumber11)
                .departureStation("西安北")
                .arrivalStation("北京西")
                .departureDateTime(getRelativeDateTime(1, 18, 30))
                .arrivalDateTime(getRelativeDateTime(1, 23, 0))
                .seatAvailability(availability11)
                .basePrice(prices11)
                .status("晚点") // 晚点状态
                .build();
        trainSchedules.put(schedule11.getScheduleId(), schedule11);
        
        // 路线 12: 成都 -> 北京 (明天直达)
        Map<SeatType, Integer> availability12 = new ConcurrentHashMap<>();
        availability12.put(SeatType.FIRST_CLASS, 12);
        availability12.put(SeatType.SECOND_CLASS, 65);
        availability12.put(SeatType.BUSINESS_CLASS, 4);
        Map<SeatType, Double> prices12 = new ConcurrentHashMap<>();
        prices12.put(SeatType.FIRST_CLASS, 1320.0);
        prices12.put(SeatType.SECOND_CLASS, 980.0);
        prices12.put(SeatType.BUSINESS_CLASS, 2100.0);
        String trainNumber12 = "G8998";
        String scheduleId12 = generateScheduleId(trainNumber12, 1); // 明天
        TrainSchedule schedule12 = TrainSchedule.builder()
                .scheduleId(scheduleId12)
                .trainNumber(trainNumber12)
                .departureStation("成都东")
                .arrivalStation("北京西")
                .departureDateTime(getRelativeDateTime(1, 7, 30))
                .arrivalDateTime(getRelativeDateTime(1, 16, 45))
                .seatAvailability(availability12)
                .basePrice(prices12)
                .status("正常")
                .build();
        trainSchedules.put(schedule12.getScheduleId(), schedule12);
        
        // 路线 13: 北京 -> 重庆 (明天)
        Map<SeatType, Integer> availability13 = new ConcurrentHashMap<>();
        availability13.put(SeatType.FIRST_CLASS, 16);
        availability13.put(SeatType.SECOND_CLASS, 70);
        Map<SeatType, Double> prices13 = new ConcurrentHashMap<>();
        prices13.put(SeatType.FIRST_CLASS, 1150.0);
        prices13.put(SeatType.SECOND_CLASS, 850.0);
        String trainNumber13 = "G571";
        String scheduleId13 = generateScheduleId(trainNumber13, 1); // 明天
        TrainSchedule schedule13 = TrainSchedule.builder()
                .scheduleId(scheduleId13)
                .trainNumber(trainNumber13)
                .departureStation("北京西")
                .arrivalStation("重庆北")
                .departureDateTime(getRelativeDateTime(1, 8, 0))
                .arrivalDateTime(getRelativeDateTime(1, 16, 30))
                .seatAvailability(availability13)
                .basePrice(prices13)
                .status("正常")
                .build();
        trainSchedules.put(schedule13.getScheduleId(), schedule13);
        
        // 路线 14: 广州 -> 杭州 (明天)
        Map<SeatType, Integer> availability14 = new ConcurrentHashMap<>();
        availability14.put(SeatType.FIRST_CLASS, 7);
        availability14.put(SeatType.SECOND_CLASS, 33);
        Map<SeatType, Double> prices14 = new ConcurrentHashMap<>();
        prices14.put(SeatType.FIRST_CLASS, 980.0);
        prices14.put(SeatType.SECOND_CLASS, 640.0);
        String trainNumber14 = "G2345";
        String scheduleId14 = generateScheduleId(trainNumber14, 1); // 明天
        TrainSchedule schedule14 = TrainSchedule.builder()
                .scheduleId(scheduleId14)
                .trainNumber(trainNumber14)
                .departureStation("广州南")
                .arrivalStation("杭州东")
                .departureDateTime(getRelativeDateTime(1, 10, 15))
                .arrivalDateTime(getRelativeDateTime(1, 18, 20))
                .seatAvailability(availability14)
                .basePrice(prices14)
                .status("正常")
                .build();
        trainSchedules.put(schedule14.getScheduleId(), schedule14);
        
        // 路线 15: 重庆 -> 成都 (今天返程)
        Map<SeatType, Integer> availability15 = new ConcurrentHashMap<>();
        availability15.put(SeatType.SECOND_CLASS, 50);
        availability15.put(SeatType.FIRST_CLASS, 10);
        Map<SeatType, Double> prices15 = new ConcurrentHashMap<>();
        prices15.put(SeatType.SECOND_CLASS, 154.0);
        prices15.put(SeatType.FIRST_CLASS, 246.0);
        String trainNumber15 = "C6003";
        String scheduleId15 = generateScheduleId(trainNumber15, 0); // 今天
        TrainSchedule schedule15 = TrainSchedule.builder()
                .scheduleId(scheduleId15)
                .trainNumber(trainNumber15)
                .departureStation("重庆北")
                .arrivalStation("成都东")
                .departureDateTime(getRelativeDateTime(0, 15, 0))
                .arrivalDateTime(getRelativeDateTime(0, 16, 30))
                .seatAvailability(availability15)
                .basePrice(prices15)
                .status("正常")
                .build();
        trainSchedules.put(schedule15.getScheduleId(), schedule15);
        
        // 路线 16: 北京 -> 上海 (后天)
        Map<SeatType, Integer> availability16 = new ConcurrentHashMap<>();
        availability16.put(SeatType.FIRST_CLASS, 20);
        availability16.put(SeatType.SECOND_CLASS, 60);
        availability16.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices16 = new ConcurrentHashMap<>();
        prices16.put(SeatType.FIRST_CLASS, 800.0);
        prices16.put(SeatType.SECOND_CLASS, 550.0);
        prices16.put(SeatType.BUSINESS_CLASS, 1600.0);
        String trainNumber16 = "G1234";
        String scheduleId16 = generateScheduleId(trainNumber16, 2); // 后天
        TrainSchedule schedule16 = TrainSchedule.builder()
                .scheduleId(scheduleId16)
                .trainNumber(trainNumber16)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(2, 9, 0))
                .arrivalDateTime(getRelativeDateTime(2, 14, 30))
                .seatAvailability(availability16)
                .basePrice(prices16)
                .status("正常")
                .build();
        trainSchedules.put(schedule16.getScheduleId(), schedule16);
        
        // 路线 17: 上海 -> 北京 (后天)
        Map<SeatType, Integer> availability17 = new ConcurrentHashMap<>();
        availability17.put(SeatType.FIRST_CLASS, 18);
        availability17.put(SeatType.SECOND_CLASS, 55);
        Map<SeatType, Double> prices17 = new ConcurrentHashMap<>();
        prices17.put(SeatType.FIRST_CLASS, 800.0);
        prices17.put(SeatType.SECOND_CLASS, 550.0);
        String trainNumber17 = "G1235";
        String scheduleId17 = generateScheduleId(trainNumber17, 2); // 后天
        TrainSchedule schedule17 = TrainSchedule.builder()
                .scheduleId(scheduleId17)
                .trainNumber(trainNumber17)
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(2, 9, 0))
                .arrivalDateTime(getRelativeDateTime(2, 14, 30))
                .seatAvailability(availability17)
                .basePrice(prices17)
                .status("正常")
                .build();
        trainSchedules.put(schedule17.getScheduleId(), schedule17);
        
        // 路线 18: 北京 -> 上海 (明天，下午班次)
        Map<SeatType, Integer> availability18 = new ConcurrentHashMap<>();
        availability18.put(SeatType.FIRST_CLASS, 10);
        availability18.put(SeatType.SECOND_CLASS, 40);
        Map<SeatType, Double> prices18 = new ConcurrentHashMap<>();
        prices18.put(SeatType.FIRST_CLASS, 780.0); // 下午班次价格略低
        prices18.put(SeatType.SECOND_CLASS, 530.0);
        String trainNumber18 = "G1236";
        String scheduleId18 = generateScheduleId(trainNumber18, 1); // 明天
        TrainSchedule schedule18 = TrainSchedule.builder()
                .scheduleId(scheduleId18)
                .trainNumber(trainNumber18)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 14, 0))
                .arrivalDateTime(getRelativeDateTime(1, 19, 30))
                .seatAvailability(availability18)
                .basePrice(prices18)
                .status("正常")
                .build();
        trainSchedules.put(schedule18.getScheduleId(), schedule18);
        
        // 路线 19: 杭州 -> 成都 (明天，长途直达)
        Map<SeatType, Integer> availability19 = new ConcurrentHashMap<>();
        availability19.put(SeatType.FIRST_CLASS, 14);
        availability19.put(SeatType.SECOND_CLASS, 48);
        availability19.put(SeatType.BUSINESS_CLASS, 6);
        Map<SeatType, Double> prices19 = new ConcurrentHashMap<>();
        prices19.put(SeatType.FIRST_CLASS, 1350.0);
        prices19.put(SeatType.SECOND_CLASS, 950.0);
        prices19.put(SeatType.BUSINESS_CLASS, 2200.0);
        String trainNumber19 = "G9876";
        String scheduleId19 = generateScheduleId(trainNumber19, 1); // 明天
        TrainSchedule schedule19 = TrainSchedule.builder()
                .scheduleId(scheduleId19)
                .trainNumber(trainNumber19)
                .departureStation("杭州东")
                .arrivalStation("成都东")
                .departureDateTime(getRelativeDateTime(1, 8, 15))
                .arrivalDateTime(getRelativeDateTime(1, 19, 45))
                .seatAvailability(availability19)
                .basePrice(prices19)
                .status("正常")
                .build();
        trainSchedules.put(schedule19.getScheduleId(), schedule19);
        
        // 路线 20: 广州 -> 北京 (明天，长途直达)
        Map<SeatType, Integer> availability20 = new ConcurrentHashMap<>();
        availability20.put(SeatType.FIRST_CLASS, 9);
        availability20.put(SeatType.SECOND_CLASS, 38);
        availability20.put(SeatType.BUSINESS_CLASS, 2);
        Map<SeatType, Double> prices20 = new ConcurrentHashMap<>();
        prices20.put(SeatType.FIRST_CLASS, 1480.0);
        prices20.put(SeatType.SECOND_CLASS, 1080.0);
        prices20.put(SeatType.BUSINESS_CLASS, 2500.0);
        String trainNumber20 = "G1010";
        String scheduleId20 = generateScheduleId(trainNumber20, 1); // 明天
        TrainSchedule schedule20 = TrainSchedule.builder()
                .scheduleId(scheduleId20)
                .trainNumber(trainNumber20)
                .departureStation("广州南")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(1, 7, 30))
                .arrivalDateTime(getRelativeDateTime(1, 19, 0))
                .seatAvailability(availability20)
                .basePrice(prices20)
                .status("正常")
                .build();
        trainSchedules.put(schedule20.getScheduleId(), schedule20);
        
        // 以下是适合改签演示的列车班次 - 相同路线不同时间
        
        // 路线 21: 北京 -> 上海 (明天晚班，适合G1234改签)
        Map<SeatType, Integer> availability21 = new ConcurrentHashMap<>();
        availability21.put(SeatType.FIRST_CLASS, 25);
        availability21.put(SeatType.SECOND_CLASS, 75);
        Map<SeatType, Double> prices21 = new ConcurrentHashMap<>();
        prices21.put(SeatType.FIRST_CLASS, 790.0);  // 晚班价格稍有不同
        prices21.put(SeatType.SECOND_CLASS, 540.0);
        String trainNumber21 = "G1238";
        String scheduleId21 = generateScheduleId(trainNumber21, 1); // 明天
        TrainSchedule schedule21 = TrainSchedule.builder()
                .scheduleId(scheduleId21)
                .trainNumber(trainNumber21)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 18, 0))
                .arrivalDateTime(getRelativeDateTime(1, 23, 30))
                .seatAvailability(availability21)
                .basePrice(prices21)
                .status("正常")
                .build();
        trainSchedules.put(schedule21.getScheduleId(), schedule21);
        
        // 路线 22: 北京 -> 上海 (后天早班，适合G1234改签)
        Map<SeatType, Integer> availability22 = new ConcurrentHashMap<>();
        availability22.put(SeatType.FIRST_CLASS, 30);
        availability22.put(SeatType.SECOND_CLASS, 80);
        Map<SeatType, Double> prices22 = new ConcurrentHashMap<>();
        prices22.put(SeatType.FIRST_CLASS, 820.0);  // 改签价格稍高
        prices22.put(SeatType.SECOND_CLASS, 570.0);
        String trainNumber22 = "G1234";
        String scheduleId22 = generateScheduleId(trainNumber22, 2) + "_early"; // 后天早班
        TrainSchedule schedule22 = TrainSchedule.builder()
                .scheduleId(scheduleId22)
                .trainNumber(trainNumber22)
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(2, 6, 30))
                .arrivalDateTime(getRelativeDateTime(2, 12, 0))
                .seatAvailability(availability22)
                .basePrice(prices22)
                .status("正常")
                .build();
        trainSchedules.put(schedule22.getScheduleId(), schedule22);
        
        // 路线 23: 上海 -> 北京 (明天不同时段，适合G1235改签)
        Map<SeatType, Integer> availability23 = new ConcurrentHashMap<>();
        availability23.put(SeatType.FIRST_CLASS, 12);
        availability23.put(SeatType.SECOND_CLASS, 45);
        Map<SeatType, Double> prices23 = new ConcurrentHashMap<>();
        prices23.put(SeatType.FIRST_CLASS, 810.0);
        prices23.put(SeatType.SECOND_CLASS, 560.0);
        String trainNumber23 = "G1237";
        String scheduleId23 = generateScheduleId(trainNumber23, 1); // 明天
        TrainSchedule schedule23 = TrainSchedule.builder()
                .scheduleId(scheduleId23)
                .trainNumber(trainNumber23)
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(1, 12, 30))
                .arrivalDateTime(getRelativeDateTime(1, 18, 0))
                .seatAvailability(availability23)
                .basePrice(prices23)
                .status("正常")
                .build();
        trainSchedules.put(schedule23.getScheduleId(), schedule23);
        
        // 路线 24: 上海 -> 北京 (明天晚班，适合G1235改签)
        Map<SeatType, Integer> availability24 = new ConcurrentHashMap<>();
        availability24.put(SeatType.FIRST_CLASS, 20);
        availability24.put(SeatType.SECOND_CLASS, 60);
        availability24.put(SeatType.BUSINESS_CLASS, 5);
        Map<SeatType, Double> prices24 = new ConcurrentHashMap<>();
        prices24.put(SeatType.FIRST_CLASS, 795.0);  // 晚班价格稍有不同
        prices24.put(SeatType.SECOND_CLASS, 545.0);
        prices24.put(SeatType.BUSINESS_CLASS, 1580.0);
        String trainNumber24 = "G1239";
        String scheduleId24 = generateScheduleId(trainNumber24, 1); // 明天
        TrainSchedule schedule24 = TrainSchedule.builder()
                .scheduleId(scheduleId24)
                .trainNumber(trainNumber24)
                .departureStation("上海虹桥")
                .arrivalStation("北京南")
                .departureDateTime(getRelativeDateTime(1, 16, 0))
                .arrivalDateTime(getRelativeDateTime(1, 21, 30))
                .seatAvailability(availability24)
                .basePrice(prices24)
                .status("正常")
                .build();
        trainSchedules.put(schedule24.getScheduleId(), schedule24);
        
        // 路线 25: 成都 -> 重庆 (明天下午，适合C6002改签)
        Map<SeatType, Integer> availability25 = new ConcurrentHashMap<>();
        availability25.put(SeatType.SECOND_CLASS, 65);
        availability25.put(SeatType.FIRST_CLASS, 12);
        Map<SeatType, Double> prices25 = new ConcurrentHashMap<>();
        prices25.put(SeatType.SECOND_CLASS, 154.0);
        prices25.put(SeatType.FIRST_CLASS, 246.0);
        String trainNumber25 = "C6004";
        String scheduleId25 = generateScheduleId(trainNumber25, 1); // 明天
        TrainSchedule schedule25 = TrainSchedule.builder()
                .scheduleId(scheduleId25)
                .trainNumber(trainNumber25)
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(getRelativeDateTime(1, 14, 0))
                .arrivalDateTime(getRelativeDateTime(1, 15, 30))
                .seatAvailability(availability25)
                .basePrice(prices25)
                .status("正常")
                .build();
        trainSchedules.put(schedule25.getScheduleId(), schedule25);
        
        // 路线 26: 成都 -> 重庆 (明天晚班，适合C6002改签)
        Map<SeatType, Integer> availability26 = new ConcurrentHashMap<>();
        availability26.put(SeatType.SECOND_CLASS, 70);
        availability26.put(SeatType.FIRST_CLASS, 18);
        Map<SeatType, Double> prices26 = new ConcurrentHashMap<>();
        prices26.put(SeatType.SECOND_CLASS, 154.0);
        prices26.put(SeatType.FIRST_CLASS, 246.0);
        String trainNumber26 = "C6006";
        String scheduleId26 = generateScheduleId(trainNumber26, 1); // 明天
        TrainSchedule schedule26 = TrainSchedule.builder()
                .scheduleId(scheduleId26)
                .trainNumber(trainNumber26)
                .departureStation("成都东")
                .arrivalStation("重庆北")
                .departureDateTime(getRelativeDateTime(1, 18, 0))
                .arrivalDateTime(getRelativeDateTime(1, 19, 30))
                .seatAvailability(availability26)
                .basePrice(prices26)
                .status("正常")
                .build();
        trainSchedules.put(schedule26.getScheduleId(), schedule26);
        
        // 路线 27: 武汉 -> 广州 (明天，不同时段，适合G1001改签)
        Map<SeatType, Integer> availability27 = new ConcurrentHashMap<>();
        availability27.put(SeatType.SECOND_CLASS, 35);
        availability27.put(SeatType.BUSINESS_CLASS, 8);
        Map<SeatType, Double> prices27 = new ConcurrentHashMap<>();
        prices27.put(SeatType.SECOND_CLASS, 490.0);  // 价格稍有调整
        prices27.put(SeatType.BUSINESS_CLASS, 1520.0);
        String trainNumber27 = "G1001";
        String scheduleId27 = generateScheduleId(trainNumber27, 1); // 明天
        TrainSchedule schedule27 = TrainSchedule.builder()
                .scheduleId(scheduleId27)
                .trainNumber(trainNumber27)
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(getRelativeDateTime(1, 10, 0))
                .arrivalDateTime(getRelativeDateTime(1, 14, 0))
                .seatAvailability(availability27)
                .basePrice(prices27)
                .status("正常")
                .build();
        trainSchedules.put(schedule27.getScheduleId(), schedule27);
        
        // 路线 28: 武汉 -> 广州 (明天晚班，适合G1001改签)
        Map<SeatType, Integer> availability28 = new ConcurrentHashMap<>();
        availability28.put(SeatType.SECOND_CLASS, 40);
        availability28.put(SeatType.BUSINESS_CLASS, 10);
        Map<SeatType, Double> prices28 = new ConcurrentHashMap<>();
        prices28.put(SeatType.SECOND_CLASS, 475.0);  // 晚班价格更低
        prices28.put(SeatType.BUSINESS_CLASS, 1490.0);
        String trainNumber28 = "G1003";
        String scheduleId28 = generateScheduleId(trainNumber28, 1); // 明天
        TrainSchedule schedule28 = TrainSchedule.builder()
                .scheduleId(scheduleId28)
                .trainNumber(trainNumber28)
                .departureStation("武汉")
                .arrivalStation("广州南")
                .departureDateTime(getRelativeDateTime(1, 16, 30))
                .arrivalDateTime(getRelativeDateTime(1, 20, 30))
                .seatAvailability(availability28)
                .basePrice(prices28)
                .status("正常")
                .build();
        trainSchedules.put(schedule28.getScheduleId(), schedule28);
        
        // 路线 29: 杭州 -> 上海 (明天，更多时间选择，适合G7502改签)
        Map<SeatType, Integer> availability29 = new ConcurrentHashMap<>();
        availability29.put(SeatType.SECOND_CLASS, 85);
        availability29.put(SeatType.FIRST_CLASS, 20);
        Map<SeatType, Double> prices29 = new ConcurrentHashMap<>();
        prices29.put(SeatType.SECOND_CLASS, 73.0);
        prices29.put(SeatType.FIRST_CLASS, 117.5);
        String trainNumber29 = "G7504";
        String scheduleId29 = generateScheduleId(trainNumber29, 1); // 明天
        TrainSchedule schedule29 = TrainSchedule.builder()
                .scheduleId(scheduleId29)
                .trainNumber(trainNumber29)
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 13, 0))
                .arrivalDateTime(getRelativeDateTime(1, 13, 50))
                .seatAvailability(availability29)
                .basePrice(prices29)
                .status("正常")
                .build();
        trainSchedules.put(schedule29.getScheduleId(), schedule29);
        
        // 路线 30: 杭州 -> 上海 (明天，晚班，适合G7502改签)
        Map<SeatType, Integer> availability30 = new ConcurrentHashMap<>();
        availability30.put(SeatType.SECOND_CLASS, 75);
        availability30.put(SeatType.FIRST_CLASS, 25);
        Map<SeatType, Double> prices30 = new ConcurrentHashMap<>();
        prices30.put(SeatType.SECOND_CLASS, 73.0);
        prices30.put(SeatType.FIRST_CLASS, 117.5);
        String trainNumber30 = "G7506";
        String scheduleId30 = generateScheduleId(trainNumber30, 1); // 明天
        TrainSchedule schedule30 = TrainSchedule.builder()
                .scheduleId(scheduleId30)
                .trainNumber(trainNumber30)
                .departureStation("杭州东")
                .arrivalStation("上海虹桥")
                .departureDateTime(getRelativeDateTime(1, 18, 30))
                .arrivalDateTime(getRelativeDateTime(1, 19, 20))
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