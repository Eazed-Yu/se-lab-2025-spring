package com.example.backend.tool;
import com.example.backend.model.User;
import com.example.backend.service.UserService;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTool {

    @Autowired
    UserService userService;

    @Tool(description = "获取用户信息")
    public User getUserInfo(String userId) {
        // 模拟获取用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }
        return user;
    }
}
