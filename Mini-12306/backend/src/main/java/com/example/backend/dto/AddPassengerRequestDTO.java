package com.example.backend.dto;

public class AddPassengerRequestDTO {
    private String name;
    private String idCard;
    private String phone;
    private Boolean isDefault;
    
    // 构造函数
    public AddPassengerRequestDTO() {}
    
    public AddPassengerRequestDTO(String name, String idCard, String phone, Boolean isDefault) {
        this.name = name;
        this.idCard = idCard;
        this.phone = phone;
        this.isDefault = isDefault;
    }
    
    // Getter和Setter方法
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
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}