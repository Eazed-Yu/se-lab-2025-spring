package com.example.backend.dto;
import lombok.Data;

@Data
public class RefundRequestDTO {
    private String userId;
    private String ticketId;
}