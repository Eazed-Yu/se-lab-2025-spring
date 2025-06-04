package com.example.backend.service.impl;

import com.example.backend.dto.PassengerDTO;
import com.example.backend.dto.AddPassengerRequestDTO;
import com.example.backend.mapper.PassengerMapper;
import com.example.backend.mapper.TicketMapper;
import com.example.backend.model.PassengerEntity;
import com.example.backend.model.TicketEntity;
import com.example.backend.service.PassengerService;
import com.example.backend.service.FileUploadService;
import com.example.backend.util.EntityConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {
    
    @Autowired
    private PassengerMapper passengerMapper;
    
    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private EntityConverter entityConverter;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Override
    @Transactional
    public PassengerDTO addPassenger(String userId, AddPassengerRequestDTO request) {
        // 检查是否已存在相同身份证号的乘车人
        PassengerEntity existing = passengerMapper.selectByUserIdAndIdCard(userId, request.getIdCard());
        if (existing != null) {
            throw new RuntimeException("该身份证号已添加过乘车人");
        }
        
        // 如果设置为默认乘车人，先取消其他默认乘车人
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            passengerMapper.clearDefaultPassenger(userId);
        }
        
        // 创建新乘车人
        PassengerEntity passenger = new PassengerEntity(
            userId,
            request.getName(),
            request.getIdCard(),
            request.getPhone(),
            request.getIsDefault() != null ? request.getIsDefault() : false
        );
        
        passengerMapper.insert(passenger);
        return entityConverter.convertToPassengerDTO(passenger);
    }
    
    @Override
    public List<PassengerDTO> getPassengersByUserId(String userId) {
        List<PassengerEntity> passengers = passengerMapper.selectByUserId(userId);
        return passengers.stream()
                .map(entityConverter::convertToPassengerDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PassengerDTO getPassengerById(String passengerId) {
        PassengerEntity passenger = passengerMapper.selectById(passengerId);
        if (passenger == null) {
            throw new RuntimeException("乘车人不存在");
        }
        return entityConverter.convertToPassengerDTO(passenger);
    }
    
    @Override
    @Transactional
    public PassengerDTO updatePassenger(String passengerId, AddPassengerRequestDTO request) {
        PassengerEntity passenger = passengerMapper.selectById(passengerId);
        if (passenger == null) {
            throw new RuntimeException("乘车人不存在");
        }
        
        // 如果身份证号发生变化，检查是否与其他乘车人冲突
        if (!passenger.getIdCard().equals(request.getIdCard())) {
            PassengerEntity existing = passengerMapper.selectByUserIdAndIdCard(passenger.getUserId(), request.getIdCard());
            if (existing != null && !existing.getId().equals(passengerId)) {
                throw new RuntimeException("该身份证号已被其他乘车人使用");
            }
        }
        
        // 如果设置为默认乘车人，先取消其他默认乘车人
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(passenger.getIsDefault())) {
            passengerMapper.clearDefaultPassenger(passenger.getUserId());
        }
        
        // 更新乘车人信息
        passenger.setName(request.getName());
        passenger.setIdCard(request.getIdCard());
        passenger.setPhone(request.getPhone());
        passenger.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        
        passengerMapper.updateById(passenger);
        return entityConverter.convertToPassengerDTO(passenger);
    }
    
    @Override
    @Transactional
    public boolean deletePassenger(String passengerId) {
        PassengerEntity passenger = passengerMapper.selectById(passengerId);
        if (passenger == null) {
            return false;
        }
        
        // 检查是否有关联的票据
        List<TicketEntity> relatedTickets = ticketMapper.selectList(
                new LambdaQueryWrapper<TicketEntity>()
                        .eq(TicketEntity::getPassengerId, passengerId)
        );
        
        if (!relatedTickets.isEmpty()) {
            throw new RuntimeException("无法删除乘车人，该乘车人存在关联的车票记录。请先处理相关车票后再删除。");
        }
        
        // 删除身份证照片文件
        if (passenger.getIdCardPhotoPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(passenger.getIdCardPhotoPath()));
            } catch (IOException e) {
                // 忽略文件删除错误
            }
        }
        
        return passengerMapper.deleteById(passengerId) > 0;
    }
    
    @Override
    @Transactional
    public boolean setDefaultPassenger(String userId, String passengerId) {
        // 验证乘车人是否属于该用户
        if (!validatePassengerOwnership(userId, passengerId)) {
            throw new RuntimeException("无权限操作该乘车人");
        }
        
        // 先取消所有默认乘车人
        passengerMapper.clearDefaultPassenger(userId);
        
        // 设置新的默认乘车人
        return passengerMapper.setDefaultPassenger(passengerId) > 0;
    }
    
    @Override
    public PassengerDTO getDefaultPassenger(String userId) {
        PassengerEntity passenger = passengerMapper.selectDefaultByUserId(userId);
        if (passenger == null) {
            return null;
        }
        return entityConverter.convertToPassengerDTO(passenger);
    }
    
    @Override
    public String uploadIdCardPhoto(String passengerId, MultipartFile file) {
        PassengerEntity passenger = passengerMapper.selectById(passengerId);
        if (passenger == null) {
            throw new RuntimeException("乘车人不存在");
        }
        
        // 删除旧文件
        if (passenger.getIdCardPhotoPath() != null) {
            fileUploadService.deleteFile(passenger.getIdCardPhotoPath());
        }
        
        // 上传新文件
        String relativePath = fileUploadService.uploadIdCardPhoto(passengerId, file);
        
        // 更新数据库
        passenger.setIdCardPhotoPath(relativePath);
        passengerMapper.updateById(passenger);
        
        return relativePath;
    }
    
    @Override
    public boolean validatePassengerOwnership(String userId, String passengerId) {
        PassengerEntity passenger = passengerMapper.selectById(passengerId);
        return passenger != null && passenger.getUserId().equals(userId);
    }
}