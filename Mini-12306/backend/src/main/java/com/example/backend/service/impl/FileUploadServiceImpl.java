package com.example.backend.service.impl;

import com.example.backend.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Value("${app.upload.path:uploads}")
    private String uploadBasePath;
    
    @Value("${app.upload.max-file-size:5MB}")
    private String maxFileSize;
    
    @Value("${app.upload.allowed-types:jpg,jpeg,png,gif}")
    private String allowedTypes;
    
    private static final String ID_CARD_SUBDIR = "id-cards";
    
    @Override
    public String uploadIdCardPhoto(String passengerId, MultipartFile file) {
        if (!validateFile(file)) {
            throw new RuntimeException("文件验证失败");
        }
        
        try {
            // 创建目录结构: uploads/id-cards/yyyy-MM/
            String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path uploadDir = Paths.get(uploadBasePath, ID_CARD_SUBDIR, yearMonth);
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成文件名: passengerId_uuid.extension
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = passengerId + "_" + UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回相对路径
            return "/uploads/" + ID_CARD_SUBDIR + "/" + yearMonth + "/" + filename;
            
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || !relativePath.startsWith("/uploads/")) {
            return false;
        }
        
        try {
            // 移除开头的 /uploads/ 前缀
            String pathWithoutPrefix = relativePath.substring(9);
            Path filePath = Paths.get(uploadBasePath, pathWithoutPrefix);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }
        
        // 验证文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase().substring(1); // 移除点号
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            return false;
        }
        
        // 验证文件大小 (5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            return false;
        }
        
        return true;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}