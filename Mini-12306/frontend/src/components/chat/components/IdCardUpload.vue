<template>
  <div class="id-card-upload">
    <div class="upload-header">
      <h4>上传身份证照片</h4>
      <div v-if="data?.passenger" class="passenger-info">
        <span>乘车人：{{ data.passenger.name }}</span>
      </div>
    </div>
    
    <el-upload
      ref="uploadRef"
      :action="uploadAction"
      :headers="uploadHeaders"
      :data="uploadData"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :before-upload="beforeUpload"
      :show-file-list="false"
      accept="image/*"
      drag
      class="upload-area"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        将身份证照片拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 jpg/png 格式，文件大小不超过 2MB
        </div>
      </template>
    </el-upload>
    
    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <el-progress :percentage="uploadProgress" :show-text="false" />
      <span class="progress-text">正在上传...</span>
    </div>
    
    <!-- 已上传的照片预览 -->
    <div v-if="uploadedPhoto" class="uploaded-preview">
      <div class="preview-header">
        <span>已上传的照片</span>
        <el-button link size="small" @click="removePhoto">删除</el-button>
      </div>
      <div class="photo-container">
        <img :src="uploadedPhoto" alt="身份证照片" @click="viewFullPhoto" />
      </div>
    </div>
    
    <!-- 识别结果 -->
    <div v-if="recognitionResult" class="recognition-result">
      <h5>识别结果</h5>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="姓名">{{ recognitionResult.name }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ recognitionResult.idCard }}</el-descriptions-item>
      </el-descriptions>
      <div class="result-actions">
        <el-button type="primary" size="small" @click="confirmRecognition">
          确认信息
        </el-button>
        <el-button size="small" @click="rejectRecognition">
          重新上传
        </el-button>
      </div>
    </div>
    
    <!-- 照片查看对话框 -->
    <el-dialog v-model="viewPhotoDialogVisible" title="身份证照片" width="600px">
      <div class="photo-viewer">
        <img :src="uploadedPhoto" alt="身份证照片" style="max-width: 100%; max-height: 400px;" />
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewPhotoDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { passengerApi } from '@/api/index.js'

const { data } = defineProps({
  data: Object,
})

const emit = defineEmits(['action'])

const uploadRef = ref(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadedPhoto = ref('')
const recognitionResult = ref(null)
const viewPhotoDialogVisible = ref(false)

// 上传配置
const uploadAction = computed(() => {
  if (data?.passenger?.id) {
    return `http://localhost:8080/api/passengers/${data.passenger.id}/id-card-photo`
  }
  return ''
})

const uploadHeaders = ref({})
const uploadData = ref({})

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  
  if (!data?.passenger?.id) {
    ElMessage.error('请先选择乘车人')
    return false
  }
  
  uploading.value = true
  uploadProgress.value = 0
  
  // 模拟上传进度
  const progressInterval = setInterval(() => {
    if (uploadProgress.value < 90) {
      uploadProgress.value += 10
    } else {
      clearInterval(progressInterval)
    }
  }, 200)
  
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  uploading.value = false
  uploadProgress.value = 100
  
  if (response.success) {
    ElMessage.success('上传成功')
    
    // 设置上传的照片
    if (response.data?.filePath) {
      uploadedPhoto.value = `http://localhost:8080${response.data.filePath}`
    }
    
    // 如果有识别结果
    if (response.data?.recognition) {
      recognitionResult.value = response.data.recognition
    }
    
    emit('action', { 
      type: 'upload_success', 
      data: response.data,
      message: '身份证照片上传成功'
    })
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error) => {
  uploading.value = false
  uploadProgress.value = 0
  ElMessage.error('上传失败，请重试')
  console.error('Upload error:', error)
}

// 确认识别结果
const confirmRecognition = () => {
  if (recognitionResult.value) {
    emit('action', { 
      type: 'confirm_recognition', 
      data: {
        passenger: data?.passenger,
        recognition: recognitionResult.value,
        photoPath: uploadedPhoto.value
      },
      message: '身份证信息识别完成，请确认乘车人信息'
    })
  }
}

// 拒绝识别结果，重新上传
const rejectRecognition = () => {
  recognitionResult.value = null
  uploadedPhoto.value = ''
  uploadProgress.value = 0
  ElMessage.info('请重新上传身份证照片')
}

// 删除照片
const removePhoto = () => {
  uploadedPhoto.value = ''
  recognitionResult.value = null
  uploadProgress.value = 0
  ElMessage.info('照片已删除')
}

// 查看完整照片
const viewFullPhoto = () => {
  viewPhotoDialogVisible.value = true
}
</script>

<style scoped>
.id-card-upload {
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.upload-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.upload-header h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.passenger-info {
  font-size: 14px;
  color: #606266;
}

.upload-area {
  margin-bottom: 16px;
}

.upload-progress {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.progress-text {
  display: block;
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.uploaded-preview {
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #303133;
}

.photo-container {
  text-align: center;
}

.photo-container img {
  max-width: 200px;
  max-height: 120px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  transition: all 0.3s ease;
}

.photo-container img:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.recognition-result {
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
}

.recognition-result h5 {
  margin: 0 0 12px 0;
  color: #409eff;
  font-size: 14px;
}

.result-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.photo-viewer {
  text-align: center;
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.el-upload__tip {
  color: #606266;
  font-size: 12px;
  margin-top: 8px;
}
</style>