package com.example.backend.tool;

import com.example.backend.dto.TicketDTO;
import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.service.TicketService;
import com.example.backend.service.ChatSessionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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


    @Tool(description = "根据用户提供的出发站、到达站和出发日期查询车次信息，并发送车次列表组件")
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

            return String.format("为您找到了 %d 趟车次，请在上方列表中选择合适的车次。", schedules.size());
        } catch (Exception e) {
            return "查询车次信息时出现错误：";
        }
    }


    @Tool(description = "显示购票表单，用于收集购票信息")
    public String showPurchaseForm(String sessionId, String trainScheduleId, String seatType) {
        try {
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("trainScheduleId", trainScheduleId);
            componentData.put("seatType", seatType);
            componentData.put("action", "purchase");

            chatSessionService.sendComponentToSession(sessionId, "ticket_form", componentData);

            return "请填写购票信息：";
        } catch (Exception e) {
            return "显示购票表单时出现错误：";
        }
    }

    @Tool(description = "显示改签表单")
    public String showChangeTicketForm(String sessionId, String ticketId) {
        try {
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("ticketId", ticketId);
            componentData.put("action", "change");

            chatSessionService.sendComponentToSession(sessionId, "change_ticket_form", componentData);

            return "请选择要改签的新车次：";
        } catch (Exception e) {
            return "显示改签表单时出现错误：";
        }
    }

    @Tool(description = "显示退票确认")
    public String showRefundConfirm(String sessionId, String ticketId) {
        try {
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("ticketId", ticketId);
            componentData.put("action", "refund");

            chatSessionService.sendComponentToSession(sessionId, "refund_confirm", componentData);

            return "请确认退票信息：";
        } catch (Exception e) {
            return "显示退票确认时出现错误：";
        }
    }


    @Tool(description = "根据用户 ID 查询用户的车票信息，并发送车票列表组件")
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
            return String.format("找到了 %d 张车票，请在上方列表中操作。", tickets.size());
        } catch (Exception e) {
            return "查询车票信息时出现错误：";
        }
    }
}