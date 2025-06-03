package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.OrderDTO;
import com.example.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 根据订单ID查询订单
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDTO> getOrderById(@PathVariable String orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        return ApiResponse.success(order);
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderDTO>> getUserOrders(@PathVariable String userId) {
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ApiResponse.success(orders);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable String orderId) {
        boolean success = orderService.cancelOrder(orderId);
        if (!success) {
            return ApiResponse.error("取消失败，订单不存在或状态不允许取消");
        }
        return ApiResponse.success(true, "取消成功");
    }
}