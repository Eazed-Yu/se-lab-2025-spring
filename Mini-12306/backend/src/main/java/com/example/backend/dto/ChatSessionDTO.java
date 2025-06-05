package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 聊天会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 消息历史列表（线程安全）
     */
    @Builder.Default
    private List<ChatMessageDTO> messages = new CopyOnWriteArrayList<>();
    
    /**
     * 会话状态：active(活跃), inactive(非活跃)
     */
    @Builder.Default
    private String status = "active";
    
    /**
     * 添加消息到会话
     */
    public void addMessage(ChatMessageDTO message) {
        this.messages.add(message);
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    /**
     * 获取最后一条消息
     */
    public ChatMessageDTO getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
}