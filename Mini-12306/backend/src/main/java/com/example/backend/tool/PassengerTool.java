package com.example.backend.tool;

import com.example.backend.dto.PassengerDTO;
import com.example.backend.service.PassengerService;
import com.example.backend.service.ChatSessionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PassengerTool {

    @Autowired
    private PassengerService passengerService;
    
    @Autowired
    private ChatSessionService chatSessionService;

    @Tool(description = "向用户发送乘车人列表")
    public String sendPassengerList(String sessionId, String userId) {
        try {
            List<PassengerDTO> passengers = passengerService.getPassengersByUserId(userId);
            
            if (passengers.isEmpty()) {
                return "您还没有添加乘车人信息，请先添加乘车人。";
            }
            
            // 发送乘车人列表组件到前端
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("passengers", passengers);
            componentData.put("action", "list");
            
            chatSessionService.sendComponentToSession(sessionId, "passenger_list", componentData);
            
            return String.format("您共有 %d 位乘车人，请在上方列表中进行管理。", passengers.size());
        } catch (Exception e) {
            return "获取乘车人列表时出现错误：" + e.getMessage();
        }
    }

    @Tool(description = "设置默认乘车人")
    public String setDefaultPassenger(String userId, String passengerId) {
        try {
            boolean success = passengerService.setDefaultPassenger(userId, passengerId);
            if (success) {
                return "默认乘车人设置成功。";
            } else {
                return "设置默认乘车人失败。";
            }
        } catch (Exception e) {
            return "设置默认乘车人时出现错误：" + e.getMessage();
        }
    }
    
}