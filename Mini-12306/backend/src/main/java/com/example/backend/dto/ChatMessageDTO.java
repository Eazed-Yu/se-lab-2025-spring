package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息类型：text(文本), component(组件), system(系统消息)
     */
    private String type;
    
    /**
     * 发送者：user(用户), assistant(AI助手)
     */
    private String sender;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 组件类型（当type为component时使用）
     * 如：train_list, ticket_form, passenger_form, file_upload等
     */
    private String componentType;
    
    /**
     * 组件数据（当type为component时使用）
     */
    private Map<String, Object> componentData;
    
    /**
     * 消息状态：sending(发送中), sent(已发送), error(错误)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 是否为流式消息的一部分
     */
    private boolean isStreaming;
    
    /**
     * 流式消息是否结束
     */
    private boolean streamEnd;
}