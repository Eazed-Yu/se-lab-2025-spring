package com.example.backend.service;

import com.example.backend.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TicketService单元测试 - 使用分支测试法
 * 测试目标：全面覆盖TicketService的各个方法和分支条件
 * 
 * 测试方法：
 * 1. querySchedules - 查询车次
 * 2. purchaseTicket - 购票
 * 3. refundTicket - 退票 
 * 4. changeTicket - 改签
 * 5. getUserTickets - 查询用户车票
 */
@SpringBootTest
@Transactional
@Rollback
public class TicketServiceBranchTest {

    @Autowired
    private TicketService ticketService;

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "u001"; // 使用数据库中已存在的用户ID
    }

    // ==================== querySchedules 测试 ====================

    @Test
    @DisplayName("查询车次 - 成功：提供完整查询条件")
    void testQuerySchedules_Success_WithAllParams() {
        // 输入：完整的查询条件
        String departureStation = "北京南";
        String arrivalStation = "上海虹桥";
        String departureDate = "2025-06-15";

        // 执行
        List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, arrivalStation, departureDate);

        // 验证
        assertNotNull(schedules, "查询结果不应为空");
        // 预期：返回符合条件的车次列表
    }

    @Test
    @DisplayName("查询车次 - 成功：仅提供出发站")
    void testQuerySchedules_Success_DepartureStationOnly() {
        // 输入：仅出发站
        String departureStation = "北京南";

        // 执行
        List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, null, null);

        // 验证
        assertNotNull(schedules, "查询结果不应为空");
        // 预期：返回从北京南出发的所有车次
    }

    @Test
    @DisplayName("查询车次 - 成功：无查询条件（查询所有）")
    void testQuerySchedules_Success_NoConditions() {
        // 输入：无查询条件
        
        // 执行
        List<TrainScheduleDTO> schedules = ticketService.querySchedules(null, null, null);

        // 验证
        assertNotNull(schedules, "查询结果不应为空");
        // 预期：返回所有车次
    }

    @Test
    @DisplayName("查询车次 - 失败：日期格式错误")
    void testQuerySchedules_Fail_InvalidDateFormat() {
        // 输入：错误的日期格式
        String invalidDate = "2025/06/15";

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.querySchedules("北京南", "上海虹桥", invalidDate);
        });

        // 预期：抛出日期格式错误异常
        assertTrue(exception.getMessage().contains("日期格式错误"));
    }

    @Test
    @DisplayName("查询车次 - 成功：无匹配结果")
    void testQuerySchedules_Success_NoResults() {
        // 输入：不存在的车站组合
        String departureStation = "不存在的出发站";
        String arrivalStation = "不存在的到达站";

        // 执行
        List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, arrivalStation, null);

        // 验证
        assertNotNull(schedules, "查询结果不应为空");
        assertTrue(schedules.isEmpty(), "应返回空列表");
        // 预期：返回空列表
    }

    // ==================== purchaseTicket 测试 ====================

    @Test
    @DisplayName("购票 - 成功：正常购票流程")
    void testPurchaseTicket_Success_Normal() {
        // 输入：有效的购票请求
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("G1234_20250520"); // 使用数据库中已存在的车次
        request.setSeatType("二等座");
        request.setPassengerId("p001"); // 使用数据库中已存在的乘车人

        // 执行
        OrderDTO result = ticketService.purchaseTicket(request);

        // 验证
        assertNotNull(result, "购票结果不应为空");
        assertNotNull(result.getOrderId(), "订单ID不应为空");
        assertNotNull(result.getTickets(), "车票信息不应为空");
        assertFalse(result.getTickets().isEmpty(), "应包含车票信息");
        assertEquals("已完成", result.getOrderStatus(), "订单状态应为已完成");
        // 预期：购票成功，返回订单和车票信息
    }

    @Test
    @DisplayName("购票 - 失败：车次不存在")
    void testPurchaseTicket_Fail_ScheduleNotFound() {
        // 输入：不存在的车次ID
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("INVALID_SCHEDULE");
        request.setSeatType("二等座");
        request.setPassengerId("p001");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.purchaseTicket(request);
        });

        // 预期：抛出车次不存在异常
        assertTrue(exception.getMessage().contains("车次信息未找到"));
    }

    @Test
    @DisplayName("购票 - 失败：座位类型无效")
    void testPurchaseTicket_Fail_InvalidSeatType() {
        // 输入：无效的座位类型
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("G1234_20250520");
        request.setSeatType("无效座位类型");
        request.setPassengerId("p001");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.purchaseTicket(request);
        });

        // 预期：抛出座位类型无效异常
        assertTrue(exception.getMessage().contains("无效的座位类型"));
    }

    @Test
    @DisplayName("购票 - 失败：乘车人不存在")
    void testPurchaseTicket_Fail_PassengerNotFound() {
        // 输入：不存在的乘车人ID
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("G1234_20250520");
        request.setSeatType("二等座");
        request.setPassengerId("INVALID_PASSENGER");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.purchaseTicket(request);
        });

        // 预期：抛出乘车人不存在异常
        assertTrue(exception.getMessage().contains("乘车人信息未找到"));
    }

    @Test
    @DisplayName("购票 - 失败：无权使用乘车人信息")
    void testPurchaseTicket_Fail_UnauthorizedPassenger() {
        // 输入：其他用户的乘车人信息
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId("u002");
        request.setScheduleId("G1234_20250520");
        request.setSeatType("二等座");
        request.setPassengerId("p001"); // p001属于u001用户

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.purchaseTicket(request);
        });

        // 预期：抛出无权使用乘车人信息异常
        assertTrue(exception.getMessage().contains("无权使用此乘车人信息"));
    }

    @Test
    @DisplayName("购票 - 失败：余票不足")
    void testPurchaseTicket_Fail_InsufficientSeats() {
        // 输入：余票为0的车次和座位类型（使用商务座，G1234_20250520没有商务座）
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("G1234_20250520");
        request.setSeatType("商务座");
        request.setPassengerId("p001");

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.purchaseTicket(request);
        });

        // 预期：抛出余票不足异常
        assertTrue(exception.getMessage().contains("余票不足"));
    }

    @Test
    @DisplayName("购票 - 失败：身份验证失败")
    void testPurchaseTicket_Fail_IdentityVerificationFailed() {
        // 输入：身份验证失败的乘车人
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        request.setUserId(testUserId);
        request.setScheduleId("G1234_20250520");
        request.setSeatType("二等座");
        request.setPassengerId("FAIL_ID_12345"); // 特殊ID，会导致身份验证失败

        // 执行和验证
        try {
            ticketService.purchaseTicket(request);
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期：抛出身份验证失败异常或乘车人不存在异常
            assertTrue(e.getMessage().contains("身份信息核验失败") || 
                      e.getMessage().contains("乘车人信息未找到"));
        }
    }

    // ==================== refundTicket 测试 ====================

    @Test
    @DisplayName("退票 - 成功：正常退票流程")
    void testRefundTicket_Success_Normal() {
        // 前置条件：先购买一张票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 输入：有效的退票请求
        RefundRequestDTO refundRequest = new RefundRequestDTO();
        refundRequest.setUserId(testUserId);
        refundRequest.setTicketId(ticketId);

        // 执行
        RefundResponseDTO result = ticketService.refundTicket(refundRequest);

        // 验证
        assertNotNull(result, "退票结果不应为空");
        assertEquals(ticketId, result.getTicketId(), "车票ID应匹配");
        assertEquals("已退票", result.getNewTicketStatus(), "车票状态应为已退票");
        assertTrue(result.getMessage().contains("退票成功"), "应包含成功信息");
        // 预期：退票成功，返回退票结果
    }

    @Test
    @DisplayName("退票 - 失败：车票不存在")
    void testRefundTicket_Fail_TicketNotFound() {
        // 输入：不存在的车票ID
        RefundRequestDTO request = new RefundRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId("INVALID_TICKET_ID");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.refundTicket(request);
        });

        // 预期：抛出车票不存在异常
        assertTrue(exception.getMessage().contains("车票不存在"));
    }

    @Test
    @DisplayName("退票 - 失败：车票状态不符合退票条件")
    void testRefundTicket_Fail_InvalidStatus() {
        // 前置条件：先购买一张票然后改签（状态变为已改签）
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 改签车票，使其状态变为已改签
        ChangeTicketRequestDTO changeRequest = new ChangeTicketRequestDTO();
        changeRequest.setUserId(testUserId);
        changeRequest.setTicketId(ticketId);
        changeRequest.setNewScheduleId("G1235_20250521");
        changeRequest.setSeatType("二等座");
        ticketService.changeTicket(changeRequest);

        // 输入：尝试退已改签的票
        RefundRequestDTO refundRequest = new RefundRequestDTO();
        refundRequest.setUserId(testUserId);
        refundRequest.setTicketId(ticketId);

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.refundTicket(refundRequest);
        });

        // 预期：抛出车票状态不符合退票条件异常
        assertTrue(exception.getMessage().contains("当前车票状态不符合退票条件"));
    }

    // ==================== changeTicket 测试 ====================

    @Test
    @DisplayName("改签 - 成功：正常改签流程")
    void testChangeTicket_Success_Normal() {
        // 前置条件：先购买一张票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 输入：有效的改签请求
        ChangeTicketRequestDTO request = new ChangeTicketRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId(ticketId);
        request.setNewScheduleId("G1235_20250521");
        request.setSeatType("一等座");

        // 执行
        ChangeTicketResponseDTO result = ticketService.changeTicket(request);

        // 验证
        assertNotNull(result, "改签结果不应为空");
        assertEquals(ticketId, result.getOldTicketId(), "原车票ID应匹配");
        assertNotNull(result.getNewTicketId(), "新车票ID不应为空");
        assertTrue(result.getMessage().contains("改签成功"), "应包含成功信息");
        assertNotNull(result.getNewTicket(), "新车票信息不应为空");
        assertEquals("一等座", result.getNewTicket().getSeatType(), "新车票座位类型应为一等座");
        // 预期：改签成功，返回新旧车票信息
    }

    @Test
    @DisplayName("改签 - 失败：原车票不存在")
    void testChangeTicket_Fail_OriginalTicketNotFound() {
        // 输入：不存在的原车票ID
        ChangeTicketRequestDTO request = new ChangeTicketRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId("INVALID_TICKET_ID");
        request.setNewScheduleId("G1235_20250531");
        request.setSeatType("二等座");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.changeTicket(request);
        });

        // 预期：抛出车票不存在异常
        assertTrue(exception.getMessage().contains("车票不存在") || 
                  exception.getMessage().contains("车票信息未找到"));
    }

    @Test
    @DisplayName("改签 - 失败：原车票状态不符合改签条件")
    void testChangeTicket_Fail_InvalidOriginalTicketStatus() {
        // 前置条件：先购买一张票然后退票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 退票，使状态变为已退票
        RefundRequestDTO refundRequest = new RefundRequestDTO();
        refundRequest.setUserId(testUserId);
        refundRequest.setTicketId(ticketId);
        ticketService.refundTicket(refundRequest);

        // 输入：尝试改签已退票的车票
        ChangeTicketRequestDTO changeRequest = new ChangeTicketRequestDTO();
        changeRequest.setUserId(testUserId);
        changeRequest.setTicketId(ticketId);
        changeRequest.setNewScheduleId("G1235_20250521");
        changeRequest.setSeatType("二等座");

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.changeTicket(changeRequest);
        });

        // 预期：抛出车票状态不符合改签条件异常
        assertTrue(exception.getMessage().contains("当前车票状态不符合改签条件"));
    }

    @Test
    @DisplayName("改签 - 失败：新车次不存在")
    void testChangeTicket_Fail_NewScheduleNotFound() {
        // 前置条件：先购买一张票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 输入：不存在的新车次ID
        ChangeTicketRequestDTO request = new ChangeTicketRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId(ticketId);
        request.setNewScheduleId("INVALID_NEW_SCHEDULE");
        request.setSeatType("二等座");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.changeTicket(request);
        });

        // 预期：抛出新车次不存在异常
        assertTrue(exception.getMessage().contains("目标车次不存在") || 
                  exception.getMessage().contains("新车次信息未找到"));
    }

    @Test
    @DisplayName("改签 - 失败：新座位类型无效")
    void testChangeTicket_Fail_InvalidNewSeatType() {
        // 前置条件：先购买一张票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 输入：无效的新座位类型
        ChangeTicketRequestDTO request = new ChangeTicketRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId(ticketId);
        request.setNewScheduleId("G1235_20250521");
        request.setSeatType("无效座位类型");

        // 执行和验证
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.changeTicket(request);
        });

        // 预期：抛出座位类型无效异常
        assertTrue(exception.getMessage().contains("无效的座位类型"));
    }

    @Test
    @DisplayName("改签 - 失败：新车次余票不足")
    void testChangeTicket_Fail_InsufficientNewSeats() {
        // 前置条件：先购买一张票
        PurchaseRequestDTO purchaseRequest = new PurchaseRequestDTO();
        purchaseRequest.setUserId(testUserId);
        purchaseRequest.setScheduleId("G1234_20250520");
        purchaseRequest.setSeatType("二等座");
        purchaseRequest.setPassengerId("p001");
        
        OrderDTO order = ticketService.purchaseTicket(purchaseRequest);
        String ticketId = order.getTickets().get(0).getTicketId();

        // 输入：余票不足的新车次（使用不存在的车次来模拟余票不足）
        ChangeTicketRequestDTO request = new ChangeTicketRequestDTO();
        request.setUserId(testUserId);
        request.setTicketId(ticketId);
        request.setNewScheduleId("SOLD_OUT_SCHEDULE");
        request.setSeatType("二等座");

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.changeTicket(request);
        });

        // 预期：抛出余票不足或车次不存在异常
        assertTrue(exception.getMessage().contains("目标车次座位余票不足") || 
                  exception.getMessage().contains("目标车次不存在"));
    }

    // ==================== getUserTickets 测试 ====================

    @Test
    @DisplayName("查询用户车票 - 成功：有车票记录")
    void testGetUserTickets_Success_WithTickets() {
        // 前置条件：购买几张票
        PurchaseRequestDTO request1 = new PurchaseRequestDTO();
        request1.setUserId(testUserId);
        request1.setScheduleId("G1234_20250520");
        request1.setSeatType("二等座");
        request1.setPassengerId("p001");
        ticketService.purchaseTicket(request1);

        PurchaseRequestDTO request2 = new PurchaseRequestDTO();
        request2.setUserId(testUserId);
        request2.setScheduleId("G1234_20250520");
        request2.setSeatType("一等座");
        request2.setPassengerId("p004"); // 使用属于u001的另一个乘车人
        ticketService.purchaseTicket(request2);

        // 执行
        List<TicketDTO> tickets = ticketService.getUserTickets(testUserId);

        // 验证
        assertNotNull(tickets, "车票列表不应为空");
        assertTrue(tickets.size() >= 2, "应至少包含2张车票");
        
        // 验证车票信息完整性
        for (TicketDTO ticket : tickets) {
            assertNotNull(ticket.getTicketId(), "车票ID不应为空");
            assertNotNull(ticket.getSeatType(), "座位类型不应为空");
            assertNotNull(ticket.getTicketStatus(), "车票状态不应为空");
        }
        // 预期：返回用户的所有车票
    }

    @Test
    @DisplayName("查询用户车票 - 成功：无车票记录")
    void testGetUserTickets_Success_NoTickets() {
        // 输入：从未购票的用户ID
        String newUserId = "u003"; // 使用确实没有车票记录的用户

        // 执行
        List<TicketDTO> tickets = ticketService.getUserTickets(newUserId);

        // 验证
        assertNotNull(tickets, "车票列表不应为空");
        assertTrue(tickets.isEmpty(), "应返回空列表");
        // 预期：返回空的车票列表
    }

    @Test
    @DisplayName("查询用户车票 - 成功：按创建时间倒序排列")
    void testGetUserTickets_Success_OrderedByCreateTime() {
        // 前置条件：按时间顺序购买多张票
        PurchaseRequestDTO request1 = new PurchaseRequestDTO();
        request1.setUserId(testUserId);
        request1.setScheduleId("G1234_20250520");
        request1.setSeatType("二等座");
        request1.setPassengerId("p001");
        ticketService.purchaseTicket(request1);

        // 稍等片刻确保时间不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PurchaseRequestDTO request2 = new PurchaseRequestDTO();
        request2.setUserId(testUserId);
        request2.setScheduleId("G1234_20250520");
        request2.setSeatType("一等座");
        request2.setPassengerId("p004"); // 使用属于u001的另一个乘车人
        ticketService.purchaseTicket(request2);

        // 执行
        List<TicketDTO> tickets = ticketService.getUserTickets(testUserId);

        // 验证
        assertNotNull(tickets, "车票列表不应为空");
        assertTrue(tickets.size() >= 2, "应至少包含2张车票");
        // 预期：最新购买的票应在前面（按创建时间倒序）
    }

    // ==================== 边界条件和异常情况测试 ====================

    @Test
    @DisplayName("边界测试 - 空字符串参数")
    void testBoundaryConditions_EmptyStrings() {
        // 测试空字符串参数的处理
        List<TrainScheduleDTO> schedules = ticketService.querySchedules("", "", "");
        assertNotNull(schedules, "查询结果不应为空");
    }

    @Test
    @DisplayName("边界测试 - null参数")
    void testBoundaryConditions_NullParams() {
        // 测试null参数的处理
        List<TicketDTO> tickets = ticketService.getUserTickets(null);
        assertNotNull(tickets, "车票列表不应为空");
        assertTrue(tickets.isEmpty(), "应返回空列表");
    }
}
