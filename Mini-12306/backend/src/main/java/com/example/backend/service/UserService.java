package com.example.backend.service;

import com.example.backend.model.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(String userId);
    
    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回null
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册成功返回用户ID，失败返回null
     */
    String register(User user);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(User user);
}