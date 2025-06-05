package com.example.backend.service;

import com.example.backend.dto.ChatMessageDTO;
import com.example.backend.dto.ChatSessionDTO;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * 聊天会话服务接口
 */
public interface ChatSessionService {
    
    /**
     * 创建新的聊天会话
     */
    ChatSessionDTO createSession(String userId);
    
    /**
     * 获取用户的所有会话
     */
    List<ChatSessionDTO> getUserSessions(String userId);
    
    /**
     * 根据会话ID获取会话
     */
    ChatSessionDTO getSession(String sessionId);
    
    /**
     * 添加消息到会话
     */
    void addMessage(String sessionId, ChatMessageDTO message);
    
    /**
     * 删除会话
     */
    boolean deleteSession(String sessionId);
    
    /**
     * 更新会话标题
     */
    boolean updateSessionTitle(String sessionId, String title);
    
    /**
     * 绑定WebSocket会话到聊天会话
     */
    void bindWebSocketSession(String sessionId, WebSocketSession webSocketSession);
    
    /**
     * 解绑WebSocket会话
     */
    void unbindWebSocketSession(String sessionId);
    
    /**
     * 获取会话对应的WebSocket连接
     */
    WebSocketSession getWebSocketSession(String sessionId);
    
    /**
     * 发送消息到指定会话
     */
    void sendMessageToSession(String sessionId, ChatMessageDTO message);
    
    /**
     * 发送组件到指定会话
     */
    void sendComponentToSession(String sessionId, String componentType, Map<String, Object> componentData);
    
    /**
     * 清理非活跃会话
     */
    void cleanupInactiveSessions();
}