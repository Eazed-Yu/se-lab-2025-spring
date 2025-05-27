package com.example.backend.service;

import com.example.backend.dto.OrderDTO;
import com.example.backend.dto.PurchaseRequestDTO;
import com.example.backend.dto.RefundRequestDTO;
import com.example.backend.dto.RefundResponseDTO;
import com.example.backend.model.Order;
import com.example.backend.model.SeatType;
import com.example.backend.model.Ticket;
import com.example.backend.model.TrainSchedule;
import com.example.backend.store.MockDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TicketServiceUnitTest {

    private MockDataStore dataStore;

    @Mock
    private PaymentService paymentService;

    @Mock
    private IdentityVerificationService identityVerificationService;

    private TicketService ticketService;

    private final String testUserId = "user123";
    private final String testScheduleId = "G1234_20250528";
    private final String testTicketId = "ticket123";
    private final String testOrderId = "order123";
    private TrainSchedule testSchedule;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 创建真实的 MockDataStore 实例
        dataStore = new MockDataStore();
        
        // 手动创建 TicketService 并注入依赖（使用反射）
        ticketService = new TicketService();
        
        // 使用反射设置私有字段
        Field dataStoreField = TicketService.class.getDeclaredField("dataStore");
        dataStoreField.setAccessible(true);
        dataStoreField.set(ticketService, dataStore);
        
        Field paymentServiceField = TicketService.class.getDeclaredField("paymentService");
        paymentServiceField.setAccessible(true);
        paymentServiceField.set(ticketService, paymentService);
        
        Field identityVerificationServiceField = TicketService.class.getDeclaredField("identityVerificationService");
        identityVerificationServiceField.setAccessible(true);
        identityVerificationServiceField.set(ticketService, identityVerificationService);
        
        // 设置测试用的 TrainSchedule
        Map<SeatType, Integer> availability = new ConcurrentHashMap<>();
        availability.put(SeatType.FIRST_CLASS, 10);
        availability.put(SeatType.SECOND_CLASS, 50);
        
        Map<SeatType, Double> prices = new ConcurrentHashMap<>();
        prices.put(SeatType.FIRST_CLASS, 800.0);
        prices.put(SeatType.SECOND_CLASS, 550.0);
        
        testSchedule = TrainSchedule.builder()
                .scheduleId(testScheduleId)
                .trainNumber("G1234")
                .departureStation("北京南")
                .arrivalStation("上海虹桥")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(5))
                .seatAvailability(availability)
                .basePrice(prices)
                .status("正常")
                .build();
        
        // 添加测试数据到 dataStore
        dataStore.trainSchedules.put(testScheduleId, testSchedule);
    }

    // 测试购票成功
    @Test
    void testPurchaseTicket_Success() {
        // 准备测试数据
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId(testScheduleId);
        request.setSeatType("二等座");
        request.setPassengerName("张三");
        request.setPassengerIdCard("110101199001011234");
        
        // 模拟服务行为
        when(identityVerificationService.verifyIdentity(anyString(), anyString())).thenReturn(true);
        when(paymentService.processPayment(anyString(), anyDouble())).thenReturn(true);
        
        // 执行测试
        OrderDTO result = ticketService.purchaseTicket(request);
        
        // 验证结果
        assertNotNull(result, "订单DTO不应为空");
        assertNotNull(result.getOrderId(), "订单ID应存在");
        assertFalse(result.getTickets().isEmpty(), "应包含车票");
        assertEquals("二等座", result.getTickets().get(0).getSeatType(), "座位类型应匹配");
        assertEquals("张三", result.getTickets().get(0).getPassengerName(), "乘客姓名应匹配");
    }
    
    // 测试购票失败 - 身份验证失败
    @Test
    void testPurchaseTicket_IdentityVerificationFailure() {
        // 准备测试数据
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId(testScheduleId);
        request.setSeatType("二等座");
        request.setPassengerName("李四");
        request.setPassengerIdCard("FAIL_ID_12345"); // 使用会导致验证失败的ID
        
        // 模拟服务行为
        when(identityVerificationService.verifyIdentity(anyString(), eq("FAIL_ID_12345"))).thenReturn(false);
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ticketService.purchaseTicket(request),
            "应抛出运行时异常");
        assertTrue(exception.getMessage().contains("身份信息核验失败"), "异常信息应包含身份验证失败");
    }
    
    // 测试退票成功
    @Test
    void testRefundTicket_Success() {
        // 准备测试数据
        Ticket ticket = Ticket.builder()
                .ticketId(testTicketId)
                .userId(testUserId)
                .orderId(testOrderId)
                .scheduleId(testScheduleId)
                .passengerName("张三")
                .passengerIdCard("110101199001011234")
                .seatType(SeatType.SECOND_CLASS)
                .ticketType("成人票")
                .pricePaid(550.0)
                .seatNumber("05车12A座")
                .ticketStatus(Ticket.Status.PAID.getDescription())
                .build();
        
        Order order = Order.builder()
                .orderId(testOrderId)
                .userId(testUserId)
                .ticketIds(Collections.singletonList(testTicketId))
                .orderCreationTime(LocalDateTime.now())
                .orderType("购票")
                .totalAmount(550.0)
                .paymentStatus(Order.PaymentStatus.SUCCESS.getDescription())
                .orderStatus(Order.Status.COMPLETED.getDescription())
                .build();
        
        dataStore.tickets.put(testTicketId, ticket);
        dataStore.orders.put(testOrderId, order);
        
        // 模拟服务行为
        when(paymentService.processRefund(anyString(), anyDouble())).thenReturn(true);
        
        // 创建测试请求
        RefundRequestDTO request = new RefundRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId(testTicketId);
        
        // 执行测试
        RefundResponseDTO result = ticketService.refundTicket(request);
        
        // 验证结果
        assertNotNull(result, "退票响应不应为空");
        assertEquals(testTicketId, result.getTicketId(), "车票ID应匹配");
        assertEquals(Ticket.Status.REFUNDED.getDescription(), result.getNewTicketStatus(), "票状态应为已退票");
        assertTrue(result.getMessage().contains("退票成功"), "应包含成功信息");
    }
}
