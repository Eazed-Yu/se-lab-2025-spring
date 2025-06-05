package com.example.backend.service;

import com.example.backend.dto.ChatMessageDTO;

/**
 * AI聊天服务接口
 */
public interface ChatAIService {
    
    /**
     * 处理用户消息，调用AI并返回响应
     */
    void processUserMessage(String sessionId, String userId, ChatMessageDTO userMessage);
    
    /**
     * 流式处理AI响应
     */
    void streamAIResponse(String sessionId, String userId, String userInput);
}