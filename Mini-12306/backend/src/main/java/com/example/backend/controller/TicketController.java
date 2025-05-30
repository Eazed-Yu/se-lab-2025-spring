package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> buyTicket(@RequestBody PurchaseRequestDTO purchaseRequest) {
        try {
            OrderDTO orderDTO = ticketService.purchaseTicket(purchaseRequest);
            return ResponseEntity.ok(orderDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("余票不足") || e.getMessage().contains("身份信息核验失败") || e.getMessage().contains("支付失败")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("购票过程中发生错误：" + e.getMessage()));
        }
    }

    // 退票
    @PostMapping("/tickets/refund")
    public ResponseEntity<?> refundTicket(@RequestBody RefundRequestDTO refundRequest) {
        try {
            RefundResponseDTO refundResponseDTO = ticketService.refundTicket(refundRequest);
            return ResponseEntity.ok(refundResponseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不符合退票条件")) {
                return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("退票过程中发生错误：" + e.getMessage()));
        }
    }

    // 查询车票
    @GetMapping("/schedules/query")
    public ResponseEntity<?> querySchedules(
            @RequestParam(required = false) String departureStation,
            @RequestParam(required = false) String arrivalStation,
            @RequestParam(required = false) String departureDate) {
        try {
            List<TrainScheduleDTO> schedules = ticketService.querySchedules(departureStation, arrivalStation, departureDate);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("查询车次时发生错误：" + e.getMessage()));
        }
    }

    // 查询用户车票
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<?> getUserTickets(@PathVariable String userId) {
        try {
            List<TicketDTO> tickets = ticketService.getUserTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("查询用户车票时发生错误：" + e.getMessage()));
        }
    }

    // 改签车票
    @PostMapping("/tickets/change")
    public ResponseEntity<?> changeTicket(@RequestBody ChangeTicketRequestDTO changeRequest) {
        try {
            ChangeTicketResponseDTO responseDTO = ticketService.changeTicket(changeRequest);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不符合改签条件") || e.getMessage().contains("余票不足")) {
                return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("改签过程中发生错误：" + e.getMessage()));
        }
    }

    static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}