package com.example.backend.dto;

import java.time.LocalDateTime;

public class PassengerDTO {
    private String id;
    private String userId;
    private String name;
    private String idCard;
    private String phone;
    private String idCardPhotoPath;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public PassengerDTO() {}
    
    public PassengerDTO(String id, String userId, String name, String idCard, String phone, Boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.idCard = idCard;
        this.phone = phone;
        this.isDefault = isDefault;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIdCard() {
        return idCard;
    }
    
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getIdCardPhotoPath() {
        return idCardPhotoPath;
    }
    
    public void setIdCardPhotoPath(String idCardPhotoPath) {
        this.idCardPhotoPath = idCardPhotoPath;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}