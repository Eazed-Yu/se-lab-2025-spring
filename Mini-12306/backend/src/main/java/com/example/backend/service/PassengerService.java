package com.example.backend.service;

import com.example.backend.dto.PassengerDTO;
import com.example.backend.dto.AddPassengerRequestDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PassengerService {
    
    /**
     * 添加乘车人
     */
    PassengerDTO addPassenger(String userId, AddPassengerRequestDTO request);
    
    /**
     * 获取用户的乘车人列表
     */
    List<PassengerDTO> getPassengersByUserId(String userId);
    
    /**
     * 根据ID获取乘车人
     */
    PassengerDTO getPassengerById(String passengerId);
    
    /**
     * 更新乘车人信息
     */
    PassengerDTO updatePassenger(String passengerId, AddPassengerRequestDTO request);
    
    /**
     * 删除乘车人
     */
    boolean deletePassenger(String passengerId);
    
    /**
     * 设置默认乘车人
     */
    boolean setDefaultPassenger(String userId, String passengerId);
    
    /**
     * 获取默认乘车人
     */
    PassengerDTO getDefaultPassenger(String userId);
    
    /**
     * 上传身份证照片
     */
    String uploadIdCardPhoto(String passengerId, MultipartFile file);
    
    /**
     * 验证乘车人是否属于指定用户
     */
    boolean validatePassengerOwnership(String userId, String passengerId);
}