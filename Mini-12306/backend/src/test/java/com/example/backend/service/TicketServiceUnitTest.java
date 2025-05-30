package com.example.backend.service;

import com.example.backend.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TicketServiceUnitTest {
    @Autowired
    private TicketService ticketService;
    // 购票并且改签成功
    @Test
    void testPurchaseTicket_Success() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        String testUserId = "user123_success";
        request.setUserId(testUserId);
        request.setScheduleId("G1235_20250530");
        request.setSeatType("二等座");
        request.setPassengerName("张三");
        request.setPassengerIdCard("110101199001011234");

        // 购票
        OrderDTO result = ticketService.purchaseTicket(request);
        ChangeTicketRequestDTO changeTicketRequestDTO = new ChangeTicketRequestDTO();
        changeTicketRequestDTO.setUserId(testUserId);
        changeTicketRequestDTO.setTicketId(result.getTickets().get(0).getTicketId());
        changeTicketRequestDTO.setNewScheduleId("G1235_20250531");
        changeTicketRequestDTO.setSeatType("商务座");

        // 改签
        ChangeTicketResponseDTO changeResponse = ticketService.changeTicket(changeTicketRequestDTO);

        // 查询
        List<TicketDTO> userTickets = ticketService.getUserTickets(testUserId);

        // 验证结果
        assertNotNull(userTickets, "购票响应不应为空");
        assertFalse(userTickets.isEmpty(), "购票响应应包含车票信息");
        TicketDTO oldTicket = userTickets.stream().filter(ticket -> ticket.getTicketId().equals(changeTicketRequestDTO.getTicketId()))
                .findFirst().orElse(null);
        assertNotNull(oldTicket, "原车票应存在");
        assertTrue(oldTicket.getTicketStatus().contains("已改签"), "原票状态应为已改签");
        
        TicketDTO newTicket = userTickets.stream().filter(ticket -> ticket.getTicketId().equals(changeResponse.getNewTicketId()))
                .findFirst().orElse(null);
        assertNotNull(newTicket, "新车票应存在");
        assertEquals("商务座", newTicket.getSeatType(), "改签后的座位类型应为商务座");
    }

    // 购票并且改签失败（不存在的座位类型）
    @Test
    void testPurchaseTicketFailed() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        String testUserId = "user123_failed";
        request.setUserId(testUserId);
        request.setScheduleId("G1235_20250530");
        request.setSeatType("二等座");
        request.setPassengerName("张三");
        request.setPassengerIdCard("110101199001011234");

        // 购票
        OrderDTO result = ticketService.purchaseTicket(request);
        ChangeTicketRequestDTO changeTicketRequestDTO = new ChangeTicketRequestDTO();
        changeTicketRequestDTO.setUserId(testUserId);
        changeTicketRequestDTO.setTicketId(result.getTickets().get(0).getTicketId());
        changeTicketRequestDTO.setNewScheduleId("G1235_20250530"); 
        changeTicketRequestDTO.setSeatType("商务座");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ticketService.changeTicket(changeTicketRequestDTO);
        }, "改签到相同班次应该抛出异常");

        assertTrue(exception.getMessage().contains("该车次不提供座位类型：商务座"));
        
        // 查询用户车票，确保原车票状态未变
        List<TicketDTO> userTickets = ticketService.getUserTickets(testUserId);
        
        // 验证结果
        assertNotNull(userTickets, "用户票列表不应为空");
        TicketDTO ticket = userTickets.stream().filter(t -> t.getTicketId().equals(changeTicketRequestDTO.getTicketId()))
                .findFirst().orElse(null);
        assertNotNull(ticket, "用户票信息应存在");
        assertFalse(ticket.getTicketStatus().contains("已改签"), "票状态不应为已改签");
        assertEquals("二等座", ticket.getSeatType(), "座位类型应保持不变");
    }

    // 购票并且改签失败（不存在的车次）
    @Test
    void testPurchaseTicketFailedNonExistentTrain() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        String testUserId = "user123_train";
        request.setUserId(testUserId);
        request.setScheduleId("G1235_20250530");
        request.setSeatType("二等座");
        request.setPassengerName("张三");
        request.setPassengerIdCard("110101199001011234");

        // 购票
        OrderDTO result = ticketService.purchaseTicket(request);
        ChangeTicketRequestDTO changeTicketRequestDTO = new ChangeTicketRequestDTO();
        changeTicketRequestDTO.setUserId(testUserId);
        changeTicketRequestDTO.setTicketId(result.getTickets().get(0).getTicketId());
        changeTicketRequestDTO.setNewScheduleId("G321_20250531");
        changeTicketRequestDTO.setSeatType("二等座");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ticketService.changeTicket(changeTicketRequestDTO);
        }, "改签到相同班次应该抛出异常");

        assertTrue(exception.getMessage().contains("新车次信息未找到：G321_20250531"));

        // 查询用户车票，确保原车票状态未变
        List<TicketDTO> userTickets = ticketService.getUserTickets(testUserId);

        // 验证结果
        assertNotNull(userTickets, "用户票列表不应为空");
        TicketDTO ticket = userTickets.stream().filter(t -> t.getTicketId().equals(changeTicketRequestDTO.getTicketId()))
                .findFirst().orElse(null);
        assertNotNull(ticket, "用户票信息应存在");
        assertFalse(ticket.getTicketStatus().contains("已改签"), "票状态不应为已改签");
        assertEquals("二等座", ticket.getSeatType(), "座位类型应保持不变");
    }

    // 购票并且改签失败（不存在的车票）
    @Test
    void testPurchaseTicketFailedNonExistentTicket() {
        PurchaseRequestDTO request = new PurchaseRequestDTO();
        String testUserId = "user123_ticket";
        request.setUserId(testUserId);
        request.setScheduleId("G1235_20250530");
        request.setSeatType("二等座");
        request.setPassengerName("张三");
        request.setPassengerIdCard("110101199001011234");
        // 购票
        OrderDTO result = ticketService.purchaseTicket(request);
        String originalTicketId = result.getTickets().get(0).getTicketId();
        
        ChangeTicketRequestDTO changeTicketRequestDTO = new ChangeTicketRequestDTO();
        changeTicketRequestDTO.setUserId(testUserId);
        changeTicketRequestDTO.setTicketId("non_existent_ticket_id");
        changeTicketRequestDTO.setNewScheduleId("G1235_20250531");
        changeTicketRequestDTO.setSeatType("二等座");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            ticketService.changeTicket(changeTicketRequestDTO);
        }, "改签不存在的车票应该抛出异常");
        assertTrue(exception.getMessage().contains("车票信息未找到："));
        // 查询用户车票，确保原车票状态未变
        List<TicketDTO> userTickets = ticketService.getUserTickets(testUserId);
        // 验证结果
        assertNotNull(userTickets, "用户票列表不应为空");
        TicketDTO ticket = userTickets.stream().filter(t -> t.getTicketId().equals(originalTicketId))
                .findFirst().orElse(null);
        assertNotNull(ticket, "原车票应存在");
        assertFalse(ticket.getTicketStatus().contains("已改签"), "票状态不应为已改签");
        assertEquals("二等座", ticket.getSeatType(), "座位类型应保持不变");
    }
}
