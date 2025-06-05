package com.example.backend.tool;

import com.example.backend.dto.TicketDTO;
import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.service.TicketService;
import com.example.backend.service.ChatSessionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
}