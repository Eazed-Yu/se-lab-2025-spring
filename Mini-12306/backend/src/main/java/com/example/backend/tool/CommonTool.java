package com.example.backend.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CommonTool {

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        // 返回IOS字符串格式的当地时间
        return java.time.LocalDateTime.now().toString();
    }
}
