package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PassengerDTO;
import com.example.backend.dto.AddPassengerRequestDTO;
import com.example.backend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@CrossOrigin(origins = "*")
public class PassengerController {
    
    @Autowired
    private PassengerService passengerService;
    
    /**
     * 添加乘车人
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PassengerDTO>> addPassenger(
            @RequestParam String userId,
            @RequestBody AddPassengerRequestDTO request) {
        try {
            PassengerDTO passenger = passengerService.addPassenger(userId, request);
            return ResponseEntity.ok(ApiResponse.success(passenger, "乘车人添加成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户的乘车人列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PassengerDTO>>> getPassengers(
            @RequestParam String userId) {
        try {
            List<PassengerDTO> passengers = passengerService.getPassengersByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(passengers, "获取乘车人列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取乘车人
     */
    @GetMapping("/{passengerId}")
    public ResponseEntity<ApiResponse<PassengerDTO>> getPassenger(
            @PathVariable String passengerId) {
        try {
            PassengerDTO passenger = passengerService.getPassengerById(passengerId);
            return ResponseEntity.ok(ApiResponse.success(passenger, "获取乘车人信息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新乘车人信息
     */
    @PutMapping("/{passengerId}")
    public ResponseEntity<ApiResponse<PassengerDTO>> updatePassenger(
            @PathVariable String passengerId,
            @RequestBody AddPassengerRequestDTO request) {
        try {
            PassengerDTO passenger = passengerService.updatePassenger(passengerId, request);
            return ResponseEntity.ok(ApiResponse.success(passenger, "乘车人信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除乘车人
     */
    @DeleteMapping("/{passengerId}")
    public ResponseEntity<ApiResponse<Boolean>> deletePassenger(
            @PathVariable String passengerId) {
        try {
            boolean success = passengerService.deletePassenger(passengerId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(true, "乘车人删除成功"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("乘车人删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 设置默认乘车人
     */
    @PutMapping("/{passengerId}/default")
    public ResponseEntity<ApiResponse<Boolean>> setDefaultPassenger(
            @PathVariable String passengerId,
            @RequestParam String userId) {
        try {
            boolean success = passengerService.setDefaultPassenger(userId, passengerId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(true, "默认乘车人设置成功"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("默认乘车人设置失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取默认乘车人
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<PassengerDTO>> getDefaultPassenger(
            @RequestParam String userId) {
        try {
            PassengerDTO passenger = passengerService.getDefaultPassenger(userId);
            return ResponseEntity.ok(ApiResponse.success(passenger, "获取默认乘车人成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 上传身份证照片
     */
    @PostMapping("/{passengerId}/id-card-photo")
    public ResponseEntity<ApiResponse<String>> uploadIdCardPhoto(
            @PathVariable String passengerId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请选择要上传的文件"));
            }
            
            String relativePath = passengerService.uploadIdCardPhoto(passengerId, file);
            return ResponseEntity.ok(ApiResponse.success(relativePath, "身份证照片上传成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}