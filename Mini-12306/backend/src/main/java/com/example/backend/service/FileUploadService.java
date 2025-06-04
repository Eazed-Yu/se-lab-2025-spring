package com.example.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    
    /**
     * 上传身份证照片
     * @param passengerId 乘车人ID
     * @param file 上传的文件
     * @return 相对路径
     */
    String uploadIdCardPhoto(String passengerId, MultipartFile file);
    
    /**
     * 删除文件
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    boolean deleteFile(String relativePath);
    
    /**
     * 验证文件类型和大小
     * @param file 上传的文件
     * @return 是否验证通过
     */
    boolean validateFile(MultipartFile file);
}