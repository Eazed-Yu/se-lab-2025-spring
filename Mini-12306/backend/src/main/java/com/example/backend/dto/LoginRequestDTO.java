package com.example.backend.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}