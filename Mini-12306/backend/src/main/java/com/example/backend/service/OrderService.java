package com.example.backend.service;

import com.example.backend.dto.OrderDTO;
import com.example.backend.model.OrderEntity;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 根据订单ID查询订单
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    OrderDTO getOrderById(String orderId);
    
    /**
     * 查询用户订单列表
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderDTO> getUserOrders(String userId);
    
    /**
     * 创建订单
     * 
     * @param order 订单信息
     * @return 创建成功返回订单ID，失败返回null
     */
    String createOrder(OrderEntity order);
    
    /**
     * 更新订单状态
     * 
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     * @param paymentStatus 支付状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateOrderStatus(String orderId, String orderStatus, String paymentStatus);
    
    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @return 取消成功返回true，失败返回false
     */
    boolean cancelOrder(String orderId);
}