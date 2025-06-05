package com.example.backend.tool;

import com.example.backend.model.User;
import com.example.backend.service.ChatSessionService;
import com.example.backend.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户相关工具类
 * 提供用户信息查询和管理功能
 */
@Component
public class UserTool {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 获取当前用户信息
     */
    @Tool(description = "获取当前用户的详细信息，包括用户名、手机号、邮箱等")
    public String getUserInfo(String userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return "用户不存在";
            }

            StringBuilder info = new StringBuilder();
            info.append("您的账户信息：\n");
            info.append("用户名：").append(user.getUsername()).append("\n");
            info.append("手机号：").append(user.getPhone() != null ? user.getPhone() : "未设置").append("\n");
            info.append("邮箱：").append(user.getEmail() != null ? user.getEmail() : "未设置").append("\n");
            info.append("注册时间：").append(user.getCreateTime()).append("\n");

            return info.toString();
        } catch (Exception e) {
            return "获取用户信息失败：" + e.getMessage();
        }
    }

    /**
     * 显示用户信息编辑表单
     */
    @Tool(description = "显示用户信息编辑表单，允许用户修改个人信息")
    public String showUserEditForm(String sessionId, String userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return "用户不存在";
            }

            // 构建用户编辑表单数据
            UserEditFormData userEditData = new UserEditFormData();
            userEditData.setId(user.getId());
            userEditData.setUsername(user.getUsername());
            userEditData.setPhone(user.getPhone());
            userEditData.setEmail(user.getEmail());

            // 发送用户编辑表单组件
            java.util.Map<String, Object> componentData = new java.util.HashMap<>();
            componentData.put("id", userEditData.getId());
            componentData.put("username", userEditData.getUsername());
            componentData.put("phone", userEditData.getPhone());
            componentData.put("email", userEditData.getEmail());
            
            chatSessionService.sendComponentToSession(
                sessionId,
                "UserEditForm",
                componentData
            );

            return "已为您显示用户信息编辑表单，您可以修改您的个人信息。";
        } catch (Exception e) {
            return "显示用户编辑表单失败：" + e.getMessage();
        }
    }

    /**
     * 显示密码修改表单
     */
    @Tool(description = "显示密码修改表单，允许用户修改登录密码")
    public String showPasswordChangeForm(String sessionId, Long userId) {
        try {
            // 发送密码修改表单组件
            PasswordChangeFormData formData = new PasswordChangeFormData();
            formData.setUserId(userId);
            
            java.util.Map<String, Object> componentData = new java.util.HashMap<>();
            componentData.put("userId", formData.getUserId());
            
            chatSessionService.sendComponentToSession(
                sessionId,
                "PasswordChangeForm",
                componentData
            );

            return "已为您显示密码修改表单，请输入当前密码和新密码。";
        } catch (Exception e) {
            return "显示密码修改表单失败：" + e.getMessage();
        }
    }

    /**
     * 显示账户安全信息
     */
    @Tool(description = "显示账户安全相关信息和设置")
    public String showAccountSecurity(String sessionId, String userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return "用户不存在";
            }

            // 构建账户安全信息
            AccountSecurityInfo securityInfo = new AccountSecurityInfo();
            securityInfo.setUserId(user.getId());
            securityInfo.setPhoneVerified(user.getPhone() != null);
            securityInfo.setEmailVerified(user.getEmail() != null);
            securityInfo.setLastUpdateTime(user.getUpdateTime());

            // 发送账户安全组件
            java.util.Map<String, Object> componentData = new java.util.HashMap<>();
            componentData.put("userId", securityInfo.getUserId());
            componentData.put("phoneVerified", securityInfo.isPhoneVerified());
            componentData.put("emailVerified", securityInfo.isEmailVerified());
            componentData.put("lastUpdateTime", securityInfo.getLastUpdateTime());
            
            chatSessionService.sendComponentToSession(
                sessionId,
                "AccountSecurity",
                componentData
            );

            return "已为您显示账户安全信息，您可以查看和管理您的安全设置。";
        } catch (Exception e) {
            return "显示账户安全信息失败：" + e.getMessage();
        }
    }

    // 组件数据类
    public static class UserEditFormData {
        private String id;
        private String username;
        private String phone;
        private String email;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class PasswordChangeFormData {
        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    public static class AccountSecurityInfo {
        private String userId;
        private boolean phoneVerified;
        private boolean emailVerified;
        private java.time.LocalDateTime lastUpdateTime;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public boolean isPhoneVerified() {
            return phoneVerified;
        }

        public void setPhoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
        }

        public java.time.LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(java.time.LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}
