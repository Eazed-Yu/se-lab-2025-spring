package com.example.backend.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String phone;
    private String email;
    
    // 默认乘车人信息
    private String passengerName;
    private String passengerIdCard;
    private String passengerPhone;
}