package com.example.backend.service.impl;

import com.example.backend.dto.ChatMessageDTO;
import com.example.backend.dto.ChatSessionDTO;
import com.example.backend.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 聊天会话服务实现类
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    
    // 内存中存储会话数据
    private final Map<String, ChatSessionDTO> sessions = new ConcurrentHashMap<>();
    
    // WebSocket会话映射
    private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public ChatSessionDTO createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        ChatSessionDTO session = ChatSessionDTO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title("新对话")
                .createTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now())
                .status("active")
                .build();
        
        sessions.put(sessionId, session);
        log.info("创建新会话: sessionId={}, userId={}", sessionId, userId);
        return session;
    }
    
    @Override
    public List<ChatSessionDTO> getUserSessions(String userId) {
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .sorted((s1, s2) -> s2.getLastUpdateTime().compareTo(s1.getLastUpdateTime()))
                .collect(Collectors.toList());
    }
    
    @Override
    public ChatSessionDTO getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    @Override
    public void addMessage(String sessionId, ChatMessageDTO message) {
        ChatSessionDTO session = sessions.get(sessionId);
        if (session != null) {
            message.setSessionId(sessionId);
            message.setTimestamp(LocalDateTime.now());
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            session.addMessage(message);
            
            // 如果是第一条用户消息，更新会话标题
            if (session.getMessages().size() == 1 && "user".equals(message.getSender())) {
                String title = message.getContent();
                if (title.length() > 20) {
                    title = title.substring(0, 20) + "...";
                }
                session.setTitle(title);
            }
            
            log.debug("添加消息到会话: sessionId={}, messageId={}", sessionId, message.getMessageId());
        }
    }
    
    @Override
    public boolean deleteSession(String sessionId) {
        ChatSessionDTO removed = sessions.remove(sessionId);
        webSocketSessions.remove(sessionId);
        log.info("删除会话: sessionId={}", sessionId);
        return removed != null;
    }
    
    @Override
    public boolean updateSessionTitle(String sessionId, String title) {
        ChatSessionDTO session = sessions.get(sessionId);
        if (session != null) {
            session.setTitle(title);
            session.setLastUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }
    
    @Override
    public void bindWebSocketSession(String sessionId, WebSocketSession webSocketSession) {
        webSocketSessions.put(sessionId, webSocketSession);
        log.debug("绑定WebSocket会话: sessionId={}", sessionId);
    }
    
    @Override
    public void unbindWebSocketSession(String sessionId) {
        webSocketSessions.remove(sessionId);
        log.debug("解绑WebSocket会话: sessionId={}", sessionId);
    }
    
    @Override
    public WebSocketSession getWebSocketSession(String sessionId) {
        return webSocketSessions.get(sessionId);
    }
    
    @Override
    public void sendMessageToSession(String sessionId, ChatMessageDTO message) {
        WebSocketSession webSocketSession = webSocketSessions.get(sessionId);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                webSocketSession.sendMessage(new TextMessage(jsonMessage));
                log.debug("发送消息到会话: sessionId={}, messageId={}", sessionId, message.getMessageId());
            } catch (Exception e) {
                log.error("发送消息失败: sessionId={}", sessionId, e);
            }
        }
    }
    
    @Override
    public void sendComponentToSession(String sessionId, String componentType, Map<String, Object> componentData) {
        ChatMessageDTO message = ChatMessageDTO.builder()
                .messageId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .type("component")
                .sender("assistant")
                .componentType(componentType)
                .componentData(componentData)
                .status("sent")
                .timestamp(LocalDateTime.now())
                .build();
        
        // 添加到会话历史
        addMessage(sessionId, message);
        
        // 发送到前端
        sendMessageToSession(sessionId, message);
    }
    
    @Override
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // 24小时未活跃的会话
        
        List<String> inactiveSessions = sessions.values().stream()
                .filter(session -> session.getLastUpdateTime().isBefore(cutoffTime))
                .map(ChatSessionDTO::getSessionId)
                .collect(Collectors.toList());
        
        for (String sessionId : inactiveSessions) {
            deleteSession(sessionId);
        }
        
        if (!inactiveSessions.isEmpty()) {
            log.info("清理非活跃会话: count={}", inactiveSessions.size());
        }
    }
}