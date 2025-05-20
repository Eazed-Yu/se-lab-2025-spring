package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class IdentityVerificationService {

    /**
     * 模拟身份验证服务
     * 
     * @param name 乘客姓名
     * @param idCardNumber 乘客身份证号
     * @return 验证结果是否通过
     */
    public boolean verifyIdentity(String name, String idCardNumber) {
        System.out.println("模拟身份认证服务：正在验证 " + name + " (" + idCardNumber + ")");
        try {
            Thread.sleep(500); // 模拟网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 演示用：除非使用特定的测试ID（FAIL_ID_12345），否则始终通过验证
        if ("FAIL_ID_12345".equals(idCardNumber)) {
            System.out.println("模拟身份认证服务：验证失败 for " + name);
            return false;
        }
        System.out.println("模拟身份认证服务：验证成功 for " + name);
        return true;
    }
}