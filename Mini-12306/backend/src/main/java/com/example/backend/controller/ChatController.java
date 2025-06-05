package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ChatSessionDTO;
import com.example.backend.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "聊天管理", description = "聊天会话管理相关接口")
public class ChatController {
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Operation(summary = "创建新的聊天会话")
    @PostMapping("/sessions")
    public ApiResponse<ChatSessionDTO> createSession(@RequestParam String userId) {
        try {
            ChatSessionDTO session = chatSessionService.createSession(userId);
            return ApiResponse.success(session);
        } catch (Exception e) {
            log.error("创建聊天会话失败: userId={}", userId, e);
            return ApiResponse.error("创建聊天会话失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取用户的所有聊天会话")
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionDTO>> getUserSessions(@RequestParam String userId) {
        try {
            List<ChatSessionDTO> sessions = chatSessionService.getUserSessions(userId);
            return ApiResponse.success(sessions);
        } catch (Exception e) {
            log.error("获取用户聊天会话失败: userId={}", userId, e);
            return ApiResponse.error("获取聊天会话失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取指定聊天会话详情")
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ChatSessionDTO> getSession(@PathVariable String sessionId) {
        try {
            ChatSessionDTO session = chatSessionService.getSession(sessionId);
            if (session == null) {
                return ApiResponse.error("会话不存在");
            }
            return ApiResponse.success(session);
        } catch (Exception e) {
            log.error("获取聊天会话详情失败: sessionId={}", sessionId, e);
            return ApiResponse.error("获取会话详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新聊天会话标题")
    @PutMapping("/sessions/{sessionId}/title")
    public ApiResponse<String> updateSessionTitle(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            if (title == null || title.trim().isEmpty()) {
                return ApiResponse.error("标题不能为空");
            }
            
            boolean success = chatSessionService.updateSessionTitle(sessionId, title.trim());
            if (success) {
                return ApiResponse.success("标题更新成功");
            } else {
                return ApiResponse.error("会话不存在");
            }
        } catch (Exception e) {
            log.error("更新聊天会话标题失败: sessionId={}", sessionId, e);
            return ApiResponse.error("更新标题失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除聊天会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<String> deleteSession(@PathVariable String sessionId) {
        try {
            boolean success = chatSessionService.deleteSession(sessionId);
            if (success) {
                return ApiResponse.success("会话删除成功");
            } else {
                return ApiResponse.error("会话不存在");
            }
        } catch (Exception e) {
            log.error("删除聊天会话失败: sessionId={}", sessionId, e);
            return ApiResponse.error("删除会话失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "清理非活跃会话")
    @PostMapping("/cleanup")
    public ApiResponse<String> cleanupInactiveSessions() {
        try {
            chatSessionService.cleanupInactiveSessions();
            return ApiResponse.success("清理完成");
        } catch (Exception e) {
            log.error("清理非活跃会话失败", e);
            return ApiResponse.error("清理失败: " + e.getMessage());
        }
    }
}