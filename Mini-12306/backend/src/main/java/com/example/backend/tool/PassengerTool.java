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

    @Tool(description = "获取用户的乘车人列表")
    public String getPassengerList(String sessionId, String userId) {
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
    
    @Tool(description = "显示添加乘车人表单")
    public String showAddPassengerForm(String sessionId) {
        try {
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("action", "add");
            
            chatSessionService.sendComponentToSession(sessionId, "passenger_form", componentData);
            
            return "请填写乘车人信息：";
        } catch (Exception e) {
            return "显示添加乘车人表单时出现错误：" + e.getMessage();
        }
    }
    
    @Tool(description = "显示编辑乘车人表单")
    public String showEditPassengerForm(String sessionId, String passengerId) {
        try {
            PassengerDTO passenger = passengerService.getPassengerById(passengerId);
            if (passenger == null) {
                return "未找到指定的乘车人信息。";
            }
            
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("action", "edit");
            componentData.put("passenger", passenger);
            
            chatSessionService.sendComponentToSession(sessionId, "passenger_form", componentData);
            
            return "请修改乘车人信息：";
        } catch (Exception e) {
            return "显示编辑乘车人表单时出现错误：" + e.getMessage();
        }
    }
    
    @Tool(description = "删除乘车人")
    public String deletePassenger(String passengerId) {
        try {
            boolean success = passengerService.deletePassenger(passengerId);
            if (success) {
                return "乘车人删除成功。";
            } else {
                return "删除乘车人失败，请检查乘车人ID是否正确。";
            }
        } catch (Exception e) {
            return "删除乘车人时出现错误：" + e.getMessage();
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
    
    @Tool(description = "显示身份证上传组件")
    public String showIdCardUpload(String sessionId, String passengerId) {
        try {
            Map<String, Object> componentData = new HashMap<>();
            componentData.put("passengerId", passengerId);
            componentData.put("uploadType", "idCard");
            
            chatSessionService.sendComponentToSession(sessionId, "file_upload", componentData);
            
            return "请上传身份证照片：";
        } catch (Exception e) {
            return "显示身份证上传组件时出现错误：" + e.getMessage();
        }
    }
}