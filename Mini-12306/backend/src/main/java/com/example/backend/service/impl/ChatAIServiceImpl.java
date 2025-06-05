package com.example.backend.service.impl;

import com.example.backend.dto.ChatMessageDTO;
import com.example.backend.dto.ChatSessionDTO;
import com.example.backend.service.ChatAIService;
import com.example.backend.service.ChatSessionService;
import com.example.backend.tool.CommonTool;
import com.example.backend.tool.PassengerTool;
import com.example.backend.tool.TicketTool;
import com.example.backend.tool.UserTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI聊天服务实现类
 */
@Slf4j
@Service
public class ChatAIServiceImpl implements ChatAIService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    PassengerTool passengerTool;

    @Autowired
    TicketTool ticketTool;

    @Autowired
    UserTool userTool;

    @Autowired
    CommonTool commonTool;

    @Override
    public void processUserMessage(String sessionId, String userId, ChatMessageDTO userMessage) {
        try {
            // 获取会话历史
            ChatSessionDTO session = chatSessionService.getSession(sessionId);
            if (session == null) {
                log.error("会话不存在: sessionId={}", sessionId);
                return;
            }

            // 构建对话历史
            List<Message> messages = buildConversationHistory(session, userId);

            // 调用AI获取响应
            String aiResponse = chatClient.prompt()
                    .tools(passengerTool)
                    .tools(ticketTool)
                    .tools(userTool)
                    .tools(commonTool)
                    .messages(messages)
                    .call()
                    .content();

            // 创建AI响应消息
            ChatMessageDTO assistantMessage = ChatMessageDTO.builder()
                    .messageId(UUID.randomUUID().toString())
                    .sessionId(sessionId)
                    .type("text")
                    .sender("assistant")
                    .content(aiResponse)
                    .status("sent")
                    .timestamp(LocalDateTime.now())
                    .build();

            // 添加到会话历史
            chatSessionService.addMessage(sessionId, assistantMessage);

            // 发送到前端
            chatSessionService.sendMessageToSession(sessionId, assistantMessage);

            log.info("AI响应发送成功: sessionId={}", sessionId);

        } catch (Exception e) {
            log.error("处理AI响应失败: sessionId={}", sessionId, e);
            throw new RuntimeException("AI处理失败", e);
        }
    }

    @Override
    public void streamAIResponse(String sessionId, String userId, String userInput) {
        try {
            // 获取会话历史
            ChatSessionDTO session = chatSessionService.getSession(sessionId);
            if (session == null) {
                log.error("会话不存在: sessionId={}", sessionId);
                return;
            }

            // 构建对话历史
            List<Message> messages = buildConversationHistory(session, userId);

            // 创建流式响应消息
            String messageId = UUID.randomUUID().toString();
            ChatMessageDTO streamMessage = ChatMessageDTO.builder()
                    .messageId(messageId)
                    .sessionId(sessionId)
                    .type("text")
                    .sender("assistant")
                    .content("")
                    .status("sending")
                    .timestamp(LocalDateTime.now())
                    .isStreaming(true)
                    .streamEnd(false)
                    .build();

            // 流式调用AI
            StringBuilder fullResponse = new StringBuilder();

            chatClient.prompt()
                    .messages(messages)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        fullResponse.append(chunk);

                        // 更新流式消息内容
                        streamMessage.setContent(fullResponse.toString());

                        // 发送到前端
                        chatSessionService.sendMessageToSession(sessionId, streamMessage);
                    })
                    .doOnComplete(() -> {
                        // 流式传输完成
                        streamMessage.setStatus("sent");
                        streamMessage.setStreamEnd(true);
                        streamMessage.setStreaming(false);

                        // 添加到会话历史
                        chatSessionService.addMessage(sessionId, streamMessage);

                        // 发送最终消息
                        chatSessionService.sendMessageToSession(sessionId, streamMessage);

                        log.info("流式AI响应完成: sessionId={}", sessionId);
                    })
                    .doOnError(error -> {
                        log.error("流式AI响应失败: sessionId={}", sessionId, error);

                        // 发送错误消息
                        streamMessage.setContent("抱歉，处理您的请求时出现了错误。");
                        streamMessage.setStatus("error");
                        streamMessage.setStreamEnd(true);
                        streamMessage.setStreaming(false);

                        chatSessionService.sendMessageToSession(sessionId, streamMessage);
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("启动流式AI响应失败: sessionId={}", sessionId, e);
            throw new RuntimeException("流式AI处理失败", e);
        }
    }

    /**
     * 构建对话历史
     */
    private List<Message> buildConversationHistory(ChatSessionDTO session, String userId) {
        List<Message> messages = new ArrayList<>();

        // 添加系统提示
        String systemPrompt = String.format(
                "你是一个智能火车票助手，可以帮助用户查询车票、购票、改签、退票以及管理乘车人信息。\n" +
                        "当前用户ID: %s\n" +
                        "当前会话ID: %s\n" +
                        "\n" +
                        "请根据用户的需求，选择合适的工具来帮助用户完成任务。\n" +
                        "注意：sessionId参数必须传递当前会话ID: %s \n"+
                "当前时间： %s",
                userId, session.getSessionId(), session.getSessionId(), LocalDateTime.now()
        );

        messages.add(new UserMessage(systemPrompt));


        for (ChatMessageDTO msg : session.getMessages()) {
            if ("text".equals(msg.getType())) {
                if ("user".equals(msg.getSender())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getSender())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }

        return messages;
    }
}