<template>
  <!-- 添加乘车人表单 -->
  <div class="passenger-form-section">
      <el-form
        ref="passengerFormRef"
        :model="passengerForm"
        :rules="passengerRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="passengerForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="passengerForm.idCard" placeholder="请输入身份证号" clearable />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="passengerForm.phone" placeholder="请输入手机号（可选）" clearable />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="passengerForm.isDefault" />
        </el-form-item>

        <el-form-item label="身份证照片">
          <div class="photo-upload-container">
            <!-- 上传区域 -->
            <div v-if="!cachedImageUrl && !passengerForm.idCardPhotoPath">
              <el-upload
                ref="formUploadRef"
                :auto-upload="false"
                :on-change="handleImageSelect"
                :before-upload="beforeUpload"
                :show-file-list="false"
                accept="image/*"
                class="simple-upload"
              >
                <div class="upload-content">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">
                    <p>点击上传身份证照片</p>
                    <p class="upload-tip">支持 JPG、PNG 格式，不超过 2MB</p>
                  </div>
                </div>
              </el-upload>
            </div>

            <!-- 照片预览区域 -->
            <div v-if="cachedImageUrl || passengerForm.idCardPhotoPath" class="photo-preview">
              <div class="photo-container">
                <img
                  :src="cachedImageUrl || getPhotoUrl(passengerForm.idCardPhotoPath)"
                  :alt="cachedImageUrl ? '身份证照片预览' : '身份证照片'"
                  class="photo-image"
                />
              </div>
              <div class="photo-actions">
                <el-button size="small" @click="handleReselect">重新选择</el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="cachedImageUrl ? removeCachedPhoto() : removePhoto()"
                >
                  删除
                </el-button>
              </div>
              <div class="photo-status">
                <span class="status-text" :class="cachedImageUrl ? 'pending' : 'uploaded'">
                  {{ cachedImageUrl ? '待上传' : '已上传' }}
                </span>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitPassenger" :loading="submitLoading">
          添加
        </el-button>
      </div>
    </div>
    <!-- 照片上传模态窗 -->
    <el-dialog
      v-model="photoDialogVisible"
      title="上传身份证照片"
      width="500px"
      :before-close="() => (photoDialogVisible = false)"
    >
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
        class="upload-demo"
      >
        <el-icon class="el-icon--upload">
          <upload-filled />
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
        </template>
      </el-upload>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="photoDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Close, UploadFilled, Clock, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { passengerApi } from '@/api/index.js'



const { data } = defineProps({
  data: Object,
})

const passengers = ref(data.passengers || [])
const loading = ref(false)
const dialogVisible = ref(false)
const photoDialogVisible = ref(false)
const submitLoading = ref(false)
const currentPassenger = ref(null)

// 表单引用
const passengerFormRef = ref(null)
const uploadRef = ref(null)
const formUploadRef = ref(null)

// 表单数据
const passengerForm = reactive({
  name: '',
  idCard: '',
  phone: '',
  isDefault: false,
  idCardPhotoPath: '',
})

// 图片缓存相关
const cachedImageFile = ref(null)
const cachedImageUrl = ref('')

// 上传配置
const uploadAction = ref('')
const uploadHeaders = ref({})
const uploadData = ref({})

// 表单上传配置
const formUploadAction = ref('')
const formUploadData = ref({})

// 表单验证规则
const validateIdCard = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入身份证号'))
  } else if (!/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的18位身份证号'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const passengerRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  idCard: [{ validator: validateIdCard, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
}

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user.id
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  ElMessage.error('请先登录')
  return null
}

// 加载乘车人列表
const loadPassengerList = async () => {
  loading.value = true
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      loading.value = false
      return
    }
    const result = await passengerApi.getPassengerList(userId)
    if (result.success) {
      passengers.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取乘车人列表失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '获取乘车人列表失败')
  } finally {
    loading.value = false
  }
}



// 提交乘车人信息
const submitPassenger = () => {
  passengerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const userId = getCurrentUserId()
        if (!userId) {
          submitLoading.value = false
          return
        }
        const passengerData = {
          ...passengerForm,
          userId,
        }

        // 先添加乘车人信息
        const result = await passengerApi.addPassenger(passengerData)

        if (result.success) {
          // 如果有缓存的图片，则上传图片
          if (cachedImageFile.value && result.data && result.data.id) {
            try {
              await uploadCachedImage(result.data.id)
              ElMessage.success('乘车人和照片添加成功')
            } catch (uploadError) {
              ElMessage.warning('乘车人添加成功，但照片上传失败：' + (uploadError.message || '上传失败'))
            }
          } else {
            ElMessage.success('添加成功')
          }

          // 关闭对话框并重新加载列表
          closeDialog()
          loadPassengerList()
        } else {
          ElMessage.error(result.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}



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
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.success) {
    ElMessage.success('上传成功')
    photoDialogVisible.value = false
    loadPassengerList()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 表单上传成功
const handleFormUploadSuccess = (response) => {
  if (response.success) {
    ElMessage.success('照片上传成功')
    // 更新表单中的照片路径
    passengerForm.idCardPhotoPath = response.data.filePath
    // 重新加载列表以更新显示
    loadPassengerList()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 表单上传失败
const handleFormUploadError = () => {
  ElMessage.error('照片上传失败')
}

// 获取照片URL
const getPhotoUrl = (photoPath) => {
  if (!photoPath) return ''
  return `http://localhost:8080${photoPath}`
}

// 删除照片
const removePhoto = () => {
  passengerForm.idCardPhotoPath = ''
}

// 重新选择照片
const handleReselect = () => {
  // 触发文件选择
  const uploadInput = formUploadRef.value?.$el.querySelector('input[type="file"]')
  if (uploadInput) {
    uploadInput.click()
  }
}

// 处理图片选择
const handleImageSelect = (file) => {
  if (beforeUpload(file.raw)) {
    cachedImageFile.value = file.raw
    // 创建本地预览URL
    cachedImageUrl.value = URL.createObjectURL(file.raw)
  }
}

// 删除缓存的照片
const removeCachedPhoto = () => {
  if (cachedImageUrl.value) {
    URL.revokeObjectURL(cachedImageUrl.value)
  }
  cachedImageFile.value = null
  cachedImageUrl.value = ''
}

// 上传缓存的图片
const uploadCachedImage = async (passengerId) => {
  if (!cachedImageFile.value) return

  const formData = new FormData()
  formData.append('file', cachedImageFile.value)

  const response = await fetch(`http://localhost:8080/api/passengers/${passengerId}/id-card-photo`, {
    method: 'POST',
    body: formData,
  })

  const result = await response.json()
  if (!result.success) {
    throw new Error(result.message || '上传失败')
  }

  return result
}

// 重置表单
const resetForm = () => {
  if (passengerFormRef.value) {
    passengerFormRef.value.resetFields()
  }
  Object.assign(passengerForm, {
    name: '',
    idCard: '',
    phone: '',
    isDefault: false,
    idCardPhotoPath: '',
  })
  currentPassenger.value = null
  // 重置上传配置
  formUploadAction.value = ''
  formUploadData.value = {}
  // 清理缓存的图片
  removeCachedPhoto()
}

// 添加乘车人
const showAddDialog = () => {
  dialogVisible.value = true
}

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
  resetForm()
}
</script>

<style scoped>
.table-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.passenger-form-section {
  margin: 16px 0;
  border-radius: 4px;
  background: #fff;
  min-width: 800px;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}

.form-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.close-btn {
  margin-left: auto;
}

.passenger-form-container {
  padding: 20px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.photo-viewer {
  text-align: center;
  margin-bottom: 20px;
}

.photo-display {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  object-fit: contain;
}

.upload-demo {
  width: 100%;
}

.upload-demo .el-upload {
  width: 100%;
}

.upload-demo .el-upload-dragger {
  width: 100%;
  height: 180px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.el-upload__tip {
  margin-top: 10px;
  color: #606266;
  font-size: 12px;
}
/* 简洁照片上传样式 */
.photo-upload-container {
  width: 100%;
}

.simple-upload {
  width: 100%;
}

.simple-upload :deep(.el-upload) {
  width: 100%;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
  cursor: pointer;
}

.simple-upload :deep(.el-upload:hover) {
  border-color: #409eff;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.upload-icon {
  font-size: 32px;
  color: #c0c4cc;
  margin-bottom: 10px;
}

.upload-text {
  color: #606266;
}

.upload-text p {
  margin: 0 0 5px 0;
  font-size: 14px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

/* 照片预览样式 */
.photo-preview {
  margin-top: 10px;
}

.photo-container {
  width: 200px;
  height: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f7fa;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.photo-status {
  margin-top: 8px;
}

.status-text {
  font-size: 12px;
}

.status-text.pending {
  color: #e6a23c;
}

.status-text.uploaded {
  color: #67c23a;
}
</style>
