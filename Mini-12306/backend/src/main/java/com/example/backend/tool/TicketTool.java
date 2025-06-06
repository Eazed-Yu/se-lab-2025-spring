package com.example.backend.tool;

import com.example.backend.dto.*;
import com.example.backend.service.TicketService;
import com.example.backend.service.ChatSessionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TicketTool {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ChatSessionService chatSessionService;


    @Tool(description = "根据用户提供的出发站、到达站和出发日期查询车次信息，向前端发送车次列表")
    public String sendTrainSchedules(String sessionId, String departureStation, String arrivalStation, String departureDate) {
        try {
            List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, arrivalStation, departureDate);

            if (schedules.isEmpty()) {
                return "未找到符合条件的车次信息。";
            }

            // 发送车次列表组件到前端
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("schedules", schedules);
            componentData.put("departureStation", departureStation);
            componentData.put("arrivalStation", arrivalStation);
            componentData.put("departureDate", departureDate);

            chatSessionService.sendComponentToSession(sessionId, "ticket_list", componentData);

            return schedules.toString();
        } catch (Exception e) {
            return "查询车次信息时出现错误：";
        }
    }


    @Tool(description = "根据用户 ID 查询用户的车票信息，并向前端发送车票列表")
    public String sendUserTickets(String sessionId, String userId) {
        try {
            List<TicketDTO> tickets = ticketService.getUserTickets(userId);

            if (tickets.isEmpty()) {
                return "您还没有购买过车票。";
            }
            // 发送车票列表组件到前端
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("tickets", tickets);
            chatSessionService.sendComponentToSession(sessionId, "user_tickets", componentData);
            return tickets.toString();
        } catch (Exception e) {
            return "查询车票信息时出现错误";
        }
    }


    @Tool(description = "购买车票，根据用户提供的乘车人信息和车次信息，向前端发送购票结果")
    public ApiResponse<?> buyTicket(String sessionId, PurchaseRequestDTO purchaseRequest) {
        try {
            OrderDTO orderDTO = ticketService.purchaseTicket(purchaseRequest);
            
            // 发送购票成功通知组件到前端
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("type", "success");
            componentData.put("title", "购票成功");
            componentData.put("message", "恭喜您，车票购买成功！请妥善保管您的车票信息。");
            
            // 从OrderDTO的tickets列表中提取信息构建订单详情
            if (orderDTO.getTickets() != null && !orderDTO.getTickets().isEmpty()) {
                Map<String, Object> orderInfo = getStringObjectMap(orderDTO);

                componentData.put("orderInfo", orderInfo);
            }
            
            chatSessionService.sendComponentToSession(sessionId, "notification", componentData);
            
            return ApiResponse.success(orderDTO, "购票成功");
        } catch (IllegalArgumentException e) {
            // 发送购票失败通知
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("type", "error");
            componentData.put("title", "购票失败");
            componentData.put("message", e.getMessage());
            
            List<Map<String, Object>> actions = new ArrayList<>();
            Map<String, Object> retryAction = new HashMap<>();
            retryAction.put("key", "retry_purchase");
            retryAction.put("label", "重新购票");
            retryAction.put("type", "primary");
            actions.add(retryAction);
            componentData.put("actions", actions);
            
            chatSessionService.sendComponentToSession(sessionId, "notification", componentData);
            
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            // 发送系统错误通知
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("type", "error");
            componentData.put("title", "系统错误");
            componentData.put("message", "购票过程中发生系统错误，请稍后重试。");
            
            chatSessionService.sendComponentToSession(sessionId, "notification", componentData);
            
            return ApiResponse.error("购票过程中发生错误：" + e.getMessage());
        }
    }

    private static Map<String, Object> getStringObjectMap(OrderDTO orderDTO) {
        TicketDTO firstTicket = orderDTO.getTickets().getFirst(); // 取第一张票的信息
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("orderNumber", orderDTO.getOrderId());

        if (firstTicket.getScheduleInfo() != null) {
            orderInfo.put("trainNumber", firstTicket.getScheduleInfo().getTrainNumber());
            orderInfo.put("departureStation", firstTicket.getScheduleInfo().getDepartureStation());
            orderInfo.put("arrivalStation", firstTicket.getScheduleInfo().getArrivalStation());
            orderInfo.put("departureTime", firstTicket.getScheduleInfo().getDepartureDateTime().toString());
        }

        orderInfo.put("seatType", firstTicket.getSeatType());
        orderInfo.put("passengerName", firstTicket.getPassengerName());
        orderInfo.put("price", firstTicket.getPricePaid());
        orderInfo.put("seatNumber", firstTicket.getSeatNumber());
        orderInfo.put("ticketCount", orderDTO.getTickets().size());
        orderInfo.put("totalAmount", orderDTO.getTotalAmount());
        return orderInfo;
    }
}