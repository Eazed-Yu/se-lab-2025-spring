package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户ID查询用户信息
     */
    @Override
    public User getUserById(String userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 根据用户名查询用户信息
     */
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 用户登录
     */
    @Override
    public User login(String username, String password) {
        // 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            return null;
        }
        
        // 验证密码
        String encryptedPassword = encryptPassword(password);
        if (!user.getPassword().equals(encryptedPassword)) {
            return null;
        }
        
        return user;
    }

    /**
     * 用户注册
     */
    @Override
    public String register(User user) {
        // 检查用户名是否已存在
        User existingUser = getUserByUsername(user.getUsername());
        if (existingUser != null) {
            return null;
        }
        
        // 设置用户ID
        String userId = UUID.randomUUID().toString().replace("-", "");
        user.setId(userId);
        
        // 加密密码
        user.setPassword(encryptPassword(user.getPassword()));
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        // 保存用户
        userMapper.insert(user);
        
        return userId;
    }

    /**
     * 更新用户信息
     */
    @Override
    public boolean updateUser(User user) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            return false;
        }
        
        // 如果更新密码，需要加密
        if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(encryptPassword(user.getPassword()));
        }
        
        // 设置更新时间
        user.setUpdateTime(LocalDateTime.now());
        
        // 更新用户
        userMapper.updateById(user);
        
        return true;
    }
    
    /**
     * 密码加密
     */
    private String encryptPassword(String password) {
        // 使用MD5加密
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}