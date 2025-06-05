package com.example.backend.websocket;

import com.example.backend.dto.ChatMessageDTO;
import com.example.backend.dto.ChatSessionDTO;
import com.example.backend.service.ChatSessionService;
import com.example.backend.service.ChatAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * WebSocket处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private ChatAIService chatAIService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立: sessionId={}", session.getId());
        
        // 从URL参数中获取用户ID和会话ID
        URI uri = session.getUri();
        String query = uri.getQuery();
        String userId = extractParam(query, "userId");
        String chatSessionId = extractParam(query, "sessionId");
        
        if (userId == null) {
            log.warn("缺少用户ID，关闭连接");
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        
        // 如果没有提供会话ID，创建新会话
        if (chatSessionId == null) {
            ChatSessionDTO chatSession = chatSessionService.createSession(userId);
            chatSessionId = chatSession.getSessionId();
        }
        
        // 绑定WebSocket会话到聊天会话
        chatSessionService.bindWebSocketSession(chatSessionId, session);
        
        // 在session中存储会话ID
        session.getAttributes().put("sessionId", chatSessionId);
        session.getAttributes().put("userId", userId);
        
        // 不再自动发送欢迎消息
        // ChatMessageDTO welcomeMessage = ChatMessageDTO.builder()
        //         .messageId(UUID.randomUUID().toString())
        //         .sessionId(chatSessionId)
        //         .type("text")
        //         .sender("assistant")
        //         .content("您好！我是您的智能助手，可以帮您查询车票、购票、改签、退票以及管理乘车人信息。请告诉我您需要什么帮助？")
        //         .status("sent")
        //         .timestamp(LocalDateTime.now())
        //         .build();
        // 
        // chatSessionService.addMessage(chatSessionId, welcomeMessage);
        // chatSessionService.sendMessageToSession(chatSessionId, welcomeMessage);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String userId = (String) session.getAttributes().get("userId");
        
        if (sessionId == null || userId == null) {
            log.warn("会话信息缺失，忽略消息");
            return;
        }
        
        try {
            // 解析用户消息
            String payload = message.getPayload().toString();
            ChatMessageDTO userMessage = objectMapper.readValue(payload, ChatMessageDTO.class);
            
            // 设置消息基本信息
            userMessage.setMessageId(UUID.randomUUID().toString());
            userMessage.setSessionId(sessionId);
            userMessage.setSender("user");
            userMessage.setStatus("sent");
            userMessage.setTimestamp(LocalDateTime.now());
            
            // 添加到会话历史
            chatSessionService.addMessage(sessionId, userMessage);
            
            log.info("收到用户消息: sessionId={}, content={}", sessionId, userMessage.getContent());
            
            // 异步处理AI响应
            CompletableFuture.runAsync(() -> {
                try {
                    chatAIService.processUserMessage(sessionId, userId, userMessage);
                } catch (Exception e) {
                    log.error("处理AI响应失败: sessionId={}", sessionId, e);
                    
                    // 发送错误消息
                    ChatMessageDTO errorMessage = ChatMessageDTO.builder()
                            .messageId(UUID.randomUUID().toString())
                            .sessionId(sessionId)
                            .type("text")
                            .sender("assistant")
                            .content("抱歉，处理您的请求时出现了错误，请稍后重试。")
                            .status("error")
                            .timestamp(LocalDateTime.now())
                            .build();
                    
                    chatSessionService.addMessage(sessionId, errorMessage);
                    chatSessionService.sendMessageToSession(sessionId, errorMessage);
                }
            });
            
        } catch (Exception e) {
            log.error("处理消息失败: sessionId={}", sessionId, e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        if (sessionId != null) {
            chatSessionService.unbindWebSocketSession(sessionId);
        }
        log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), closeStatus);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 从查询字符串中提取参数
     */
    private String extractParam(String query, String paramName) {
        if (query == null) {
            return null;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}