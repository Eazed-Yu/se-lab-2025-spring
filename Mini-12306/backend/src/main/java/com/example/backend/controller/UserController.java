package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequestDTO request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }
        
        // 不返回密码
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequestDTO request) {
        // 检查参数
        if (request.getUsername() == null || request.getPassword() == null) {
            return ApiResponse.error("用户名和密码不能为空");
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setIdCard(request.getIdCard());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        
        // 注册用户
        String userId = userService.register(user);
        if (userId == null) {
            return ApiResponse.error("用户名已存在");
        }
        
        return ApiResponse.success(userId, "注册成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserInfo(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        // 不返回密码
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ApiResponse<Boolean> updateUserInfo(@PathVariable String userId, @RequestBody User user) {
        // 设置用户ID
        user.setId(userId);
        
        // 更新用户信息
        boolean success = userService.updateUser(user);
        if (!success) {
            return ApiResponse.error("更新失败，用户不存在");
        }
        
        return ApiResponse.success(true, "更新成功");
    }
}