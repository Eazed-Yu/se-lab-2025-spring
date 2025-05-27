package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {


    /**
     * 模拟处理支付
     * 
     * @param orderId 订单ID
     * @param amount 支付金额
     * @return 支付是否成功
     */
    public boolean processPayment(String orderId, double amount) {
        System.out.println("模拟支付服务：订单 " + orderId + " 正在支付金额：" + amount);
        try {
            Thread.sleep(1500); // 模拟支付处理的网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
   
        boolean success = true;

        if (success) {
            System.out.println("模拟支付服务：订单 " + orderId + " 支付成功");
        } else {
            System.out.println("模拟支付服务：订单 " + orderId + " 支付失败");
        }
        return success;
    }

    /**
     * 模拟处理退款
     * 
     * @param ticketId 车票ID
     * @param amount 退款金额
     * @return 退款是否成功
     */
    public boolean processRefund(String ticketId, double amount) {
        System.out.println("模拟支付服务：车票 " + ticketId + " 正在退款金额：" + amount);
        try {
            Thread.sleep(1000); // 模拟网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("模拟支付服务：车票 " + ticketId + " 退款成功");
        return true; // 演示用：假设退款处理始终成功
    }
}