package com.example.backend.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String phone;
    private String email;
}