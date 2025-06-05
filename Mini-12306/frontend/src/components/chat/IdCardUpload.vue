<template>
  <div class="id-card-upload">
    <div class="upload-header">
      <h3>身份证上传</h3>
      <p class="tip">请上传身份证正反面照片，确保信息清晰可见</p>
    </div>
    
    <div class="upload-container">
      <!-- 正面上传 -->
      <div class="upload-item">
        <div class="upload-label">
          <el-icon><CreditCard /></el-icon>
          身份证正面
        </div>
        <el-upload
          class="id-card-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="(response) => handleSuccess(response, 'front')"
          :on-error="handleError"
          accept="image/*"
        >
          <div v-if="frontImage" class="image-preview">
            <img :src="frontImage" alt="身份证正面" />
            <div class="image-overlay">
              <el-button type="primary" size="small">重新上传</el-button>
            </div>
          </div>
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">
              <p>点击上传身份证正面</p>
              <p class="upload-hint">支持 JPG、PNG 格式，大小不超过 5MB</p>
            </div>
          </div>
        </el-upload>
      </div>
      
      <!-- 反面上传 -->
      <div class="upload-item">
        <div class="upload-label">
          <el-icon><CreditCard /></el-icon>
          身份证反面
        </div>
        <el-upload
          class="id-card-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="(response) => handleSuccess(response, 'back')"
          :on-error="handleError"
          accept="image/*"
        >
          <div v-if="backImage" class="image-preview">
            <img :src="backImage" alt="身份证反面" />
            <div class="image-overlay">
              <el-button type="primary" size="small">重新上传</el-button>
            </div>
          </div>
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">
              <p>点击上传身份证反面</p>
              <p class="upload-hint">支持 JPG、PNG 格式，大小不超过 5MB</p>
            </div>
          </div>
        </el-upload>
      </div>
    </div>
    
    <!-- 识别结果 -->
    <div v-if="ocrResult" class="ocr-result">
      <h4>识别结果</h4>
      <el-form :model="ocrResult" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="ocrResult.name" readonly />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="ocrResult.idNumber" readonly />
        </el-form-item>
        <el-form-item label="性别">
          <el-input v-model="ocrResult.gender" readonly />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-input v-model="ocrResult.birthDate" readonly />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="ocrResult.address" type="textarea" readonly />
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 操作按钮 -->
    <div class="actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm" 
        :disabled="!frontImage || !backImage"
        :loading="uploading"
      >
        {{ uploading ? '处理中...' : '确认上传' }}
      </el-button>
    </div>
    
    <!-- 上传提示 -->
    <div class="upload-tips">
      <h4>上传提示</h4>
      <ul>
        <li>请确保身份证照片清晰，四角完整</li>
        <li>避免反光、模糊、倾斜等情况</li>
        <li>支持自动识别身份证信息</li>
        <li>上传的照片仅用于实名认证，不会泄露</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Plus, CreditCard } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['action']);

const frontImage = ref('');
const backImage = ref('');
const ocrResult = ref(null);
const uploading = ref(false);

const uploadUrl = '/api/upload/id-card';

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  
  uploading.value = true;
  return true;
};

const handleSuccess = (response, side) => {
  uploading.value = false;
  
  if (response.success) {
    if (side === 'front') {
      frontImage.value = response.data.imageUrl;
    } else {
      backImage.value = response.data.imageUrl;
    }
    
    // 如果有OCR识别结果
    if (response.data.ocrResult) {
      ocrResult.value = response.data.ocrResult;
    }
    
    ElMessage.success(`身份证${side === 'front' ? '正面' : '反面'}上传成功`);
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const handleError = (error) => {
  uploading.value = false;
  console.error('上传失败:', error);
  ElMessage.error('上传失败，请重试');
};

const handleConfirm = () => {
  const uploadData = {
    frontImage: frontImage.value,
    backImage: backImage.value,
    ocrResult: ocrResult.value
  };
  
  emit('action', {
    type: 'confirm_id_card_upload',
    data: uploadData
  });
};

const handleCancel = () => {
  emit('action', {
    type: 'cancel_id_card_upload'
  });
};
</script>

<style scoped>
.id-card-upload {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.upload-header {
  text-align: center;
  margin-bottom: 30px;
}

.upload-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.tip {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.upload-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.upload-item {
  text-align: center;
}

.upload-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #303133;
}

.id-card-uploader {
  width: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 180px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 32px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-text p {
  margin: 0;
  color: #606266;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .image-overlay {
  opacity: 1;
}

.ocr-result {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.ocr-result h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 30px;
}

.upload-tips {
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.upload-tips h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.upload-tips ul {
  margin: 0;
  padding-left: 20px;
}

.upload-tips li {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 4px;
}

@media (max-width: 768px) {
  .upload-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .upload-placeholder {
    height: 150px;
  }
  
  .image-preview {
    height: 150px;
  }
}
</style>