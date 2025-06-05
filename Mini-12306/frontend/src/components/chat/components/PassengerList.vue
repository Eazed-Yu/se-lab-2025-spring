<template>
  <div class="passenger-list">
    <div class="list-header">
      <h4>乘车人列表</h4>
      <el-button type="primary" size="small" @click="addPassenger" icon="Plus">
        添加乘车人
      </el-button>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="passengers.length === 0" class="empty">
      <el-empty description="暂无乘车人信息" />
    </div>
    
    <div v-else class="passenger-cards">
      <div 
        v-for="passenger in passengers" 
        :key="passenger.id" 
        class="passenger-card"
      >
        <div class="card-content">
          <div class="passenger-info">
            <div class="name-section">
              <span class="name">{{ passenger.name }}</span>
              <el-tag v-if="passenger.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <div class="id-card">{{ passenger.idCard }}</div>
            <div v-if="passenger.phone" class="phone">{{ passenger.phone }}</div>
            <div class="create-time">创建时间：{{ formatDate(passenger.createTime) }}</div>
          </div>
          
          <div class="card-actions">
            <el-button 
              v-if="!passenger.isDefault"
              link 
              size="small" 
              @click="setDefault(passenger.id)"
            >
              设为默认
            </el-button>
            <el-button 
              link 
              size="small" 
              @click="editPassenger(passenger)"
            >
              编辑
            </el-button>
            <el-button 
              link 
              size="small" 
              @click="deletePassenger(passenger.id)"
              style="color: #f56c6c"
            >
              删除
            </el-button>
          </div>
        </div>
        
        <div class="photo-section">
          <div v-if="passenger.idCardPhotoPath" class="photo-preview">
            <img 
              :src="getPhotoUrl(passenger.idCardPhotoPath)" 
              alt="身份证照片" 
              @click="viewPhoto(passenger.idCardPhotoPath)"
            />
          </div>
          <el-button 
            v-else 
            size="small" 
            @click="uploadPhoto(passenger)"
          >
            上传照片
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 照片查看对话框 -->
    <el-dialog v-model="viewPhotoDialogVisible" title="身份证照片" width="600px">
      <div class="photo-viewer">
        <img :src="currentPhotoUrl" alt="身份证照片" style="max-width: 100%; max-height: 400px;" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { passengerApi } from '@/api/index.js'

const { data } = defineProps({
  data: Object,
})

const emit = defineEmits(['action'])

const passengers = ref(data?.passengers || [])
const loading = ref(false)
const viewPhotoDialogVisible = ref(false)
const currentPhotoUrl = ref('')

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

// 添加乘车人
const addPassenger = () => {
  emit('action', { 
    type: 'add_passenger',
    message: '请填写乘车人信息'
  })
}

// 编辑乘车人
const editPassenger = (passenger) => {
  emit('action', { 
    type: 'edit_passenger', 
    data: passenger,
    message: `正在编辑乘车人：${passenger.name}`
  })
}

// 删除乘车人
const deletePassenger = async (passengerId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个乘车人吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await passengerApi.deletePassenger(passengerId)
    if (result.success) {
      ElMessage.success('删除成功')
      loadPassengerList()
      emit('action', { 
        type: 'delete_passenger',
        message: '乘车人已删除'
      })
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 设置默认乘车人
const setDefault = async (passengerId) => {
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      return
    }
    const result = await passengerApi.setDefaultPassenger(userId, passengerId)
    if (result.success) {
      ElMessage.success('设置成功')
      loadPassengerList()
      emit('action', { 
        type: 'set_default',
        message: '默认乘车人已设置'
      })
    } else {
      ElMessage.error(result.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '设置失败')
  }
}

// 上传照片
const uploadPhoto = (passenger) => {
  emit('action', { 
    type: 'upload_photo', 
    data: passenger,
    message: `为 ${passenger.name} 上传身份证照片`
  })
}

// 查看照片
const viewPhoto = (photoPath) => {
  currentPhotoUrl.value = `http://localhost:8080${photoPath}`
  viewPhotoDialogVisible.value = true
}

// 获取照片URL
const getPhotoUrl = (photoPath) => {
  if (!photoPath) return ''
  return `http://localhost:8080${photoPath}`
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  if (!data?.passengers) {
    loadPassengerList()
  }
})
</script>

<style scoped>
.passenger-list {
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.list-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.loading {
  padding: 20px;
}

.empty {
  padding: 40px 20px;
  text-align: center;
}

.passenger-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.passenger-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.passenger-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.passenger-info {
  flex: 1;
}

.name-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.id-card {
  font-size: 14px;
  color: #606266;
  font-family: monospace;
}

.phone {
  font-size: 14px;
  color: #606266;
}

.create-time {
  font-size: 12px;
  color: #909399;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.photo-section {
  display: flex;
  align-items: center;
  margin-left: 16px;
}

.photo-preview {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  cursor: pointer;
}

.photo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-viewer {
  text-align: center;
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>