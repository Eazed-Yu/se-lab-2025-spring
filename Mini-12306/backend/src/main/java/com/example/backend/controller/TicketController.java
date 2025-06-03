package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api") 
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // 认证失败的测试ID（FAIL_ID_12345）
    @PostMapping("/tickets/buy")
    public ApiResponse<?> buyTicket(@RequestBody PurchaseRequestDTO purchaseRequest) {
        try {
            OrderDTO orderDTO = ticketService.purchaseTicket(purchaseRequest);
            return ApiResponse.success(orderDTO, "购票成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error("购票过程中发生错误：" + e.getMessage());
        }
    }

    // 退票
    @PostMapping("/tickets/refund")
    public ApiResponse<?> refundTicket(@RequestBody RefundRequestDTO refundRequest) {
        try {
            RefundResponseDTO refundResponseDTO = ticketService.refundTicket(refundRequest);
            return ApiResponse.success(refundResponseDTO, "退票成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error("退票过程中发生错误：" + e.getMessage());
        }
    }

    // 查询车票
    @GetMapping("/schedules/query")
    public ApiResponse<List<TrainScheduleDTO>> querySchedules(
            @RequestParam(required = false) String departureStation,
            @RequestParam(required = false) String arrivalStation,
            @RequestParam(required = false) String departureDate) {
        try {
            List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, arrivalStation, departureDate);
            return ApiResponse.success(schedules);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("查询车次时发生错误：" + e.getMessage());
        }
    }

    // 查询用户车票
    @GetMapping("/tickets/user/{userId}")
    public ApiResponse<List<TicketDTO>> getUserTickets(@PathVariable String userId) {
        try {
            List<TicketDTO> tickets = ticketService.getUserTickets(userId);
            return ApiResponse.success(tickets);
        } catch (Exception e) {
            return ApiResponse.error("查询用户车票时发生错误：" + e.getMessage());
        }
    }

    // 改签车票
    @PostMapping("/tickets/change")
    public ApiResponse<?> changeTicket(@RequestBody ChangeTicketRequestDTO changeRequest) {
        try {
            ChangeTicketResponseDTO responseDTO = ticketService.changeTicket(changeRequest);
            return ApiResponse.success(responseDTO, "改签成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error("改签过程中发生错误：" + e.getMessage());
        }
    }
}