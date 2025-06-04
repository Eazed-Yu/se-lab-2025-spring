<template>
  <div class="passenger-management">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h3>乘车人管理</h3>
          <el-button type="primary" @click="showAddDialog" icon="Plus">
            添加乘车人
          </el-button>
        </div>
      </template>
      
      <!-- 乘车人列表 -->
      <el-table 
        :data="passengerList" 
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无乘车人信息"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="idCard" label="身份证号" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="默认乘车人" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault" type="success">默认</el-tag>
            <el-button 
              v-else 
              link 
              size="small" 
              @click="setDefault(scope.row.id)"
            >
              设为默认
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="身份证照片" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.idCardPhotoPath" 
              link 
              size="small" 
              @click="viewPhoto(scope.row.idCardPhotoPath)"
            >
              查看照片
            </el-button>
            <el-button 
              v-else 
              link 
              size="small" 
              @click="uploadPhoto(scope.row)"
            >
              上传照片
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              link 
              size="small" 
              @click="editPassenger(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              link 
              size="small" 
              @click="deletePassenger(scope.row.id)"
              style="color: #f56c6c"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑乘车人对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑乘车人' : '添加乘车人'" 
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="passengerFormRef" 
        :model="passengerForm" 
        :rules="passengerRules" 
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="passengerForm.name" 
            placeholder="请输入姓名" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input 
            v-model="passengerForm.idCard" 
            placeholder="请输入身份证号" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="passengerForm.phone" 
            placeholder="请输入手机号（可选）" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="passengerForm.isDefault" />
        </el-form-item>
        
        <el-form-item label="身份证照片">
          <el-upload
            ref="formUploadRef"
            :action="formUploadAction"
            :headers="uploadHeaders"
            :data="uploadData"
            :on-success="handleFormUploadSuccess"
            :on-error="handleFormUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept="image/*"
            :disabled="!currentPassenger || !currentPassenger.id"
          >
            <el-button size="small" type="primary" :disabled="!currentPassenger || !currentPassenger.id">
              {{ passengerForm.idCardPhotoPath ? '重新上传' : '上传照片' }}
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ !currentPassenger || !currentPassenger.id ? '请先保存乘车人信息后再上传照片' : '只能上传jpg/png文件，且不超过2MB' }}
              </div>
            </template>
          </el-upload>
          
          <!-- 显示已上传的照片 -->
          <div v-if="passengerForm.idCardPhotoPath" class="uploaded-photo">
            <img :src="getPhotoUrl(passengerForm.idCardPhotoPath)" alt="身份证照片" class="photo-preview" />
            <el-button size="small" link @click="removePhoto">删除照片</el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPassenger" :loading="submitLoading">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 照片上传对话框 -->
    <el-dialog v-model="photoDialogVisible" title="上传身份证照片" width="400px">
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
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传jpg/png文件，且不超过2MB
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="photoDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, UploadFilled } from '@element-plus/icons-vue';
import { passengerApi } from '../api';

// 响应式数据
const loading = ref(false);
const passengerList = ref([]);
const dialogVisible = ref(false);
const photoDialogVisible = ref(false);
const viewPhotoDialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const currentPassenger = ref(null);
const currentPhotoUrl = ref('');

// 表单引用
const passengerFormRef = ref(null);
const uploadRef = ref(null);
const formUploadRef = ref(null);

// 表单数据
const passengerForm = reactive({
  name: '',
  idCard: '',
  phone: '',
  isDefault: false,
  idCardPhotoPath: ''
});

// 上传配置
const uploadAction = ref('');
const uploadHeaders = ref({});
const uploadData = ref({});

// 表单上传配置
const formUploadAction = ref('');
const formUploadHeaders = ref({});
const formUploadData = ref({});

// 表单验证规则
const validateIdCard = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入身份证号'));
  } else if (!/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的18位身份证号'));
  } else {
    callback();
  }
};

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'));
  } else {
    callback();
  }
};

const passengerRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  idCard: [
    { validator: validateIdCard, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
};

// 获取当前用户ID（实际项目中应该从用户状态管理中获取）
const getCurrentUserId = () => {
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.id;
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
  // 如果没有登录用户，返回null或重定向到登录页
  ElMessage.error('请先登录');
  return null;
};

// 加载乘车人列表
const loadPassengerList = async () => {
  loading.value = true;
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      loading.value = false;
      return;
    }
    const result = await passengerApi.getPassengerList(userId);
    if (result.success) {
      passengerList.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取乘车人列表失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '获取乘车人列表失败');
  } finally {
    loading.value = false;
  }
};

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false;
  dialogVisible.value = true;
};

// 编辑乘车人
const editPassenger = (passenger) => {
  isEdit.value = true;
  currentPassenger.value = passenger;
  Object.assign(passengerForm, {
    name: passenger.name,
    idCard: passenger.idCard,
    phone: passenger.phone || '',
    isDefault: passenger.isDefault,
    idCardPhotoPath: passenger.idCardPhotoPath || ''
  });
  // 设置表单上传配置
  if (passenger.id) {
    formUploadAction.value = `http://localhost:8080/api/passengers/${passenger.id}/id-card-photo`;
    formUploadData.value = {};
  }
  dialogVisible.value = true;
};

// 提交乘车人信息
const submitPassenger = () => {
  passengerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const userId = getCurrentUserId();
        if (!userId) {
          submitLoading.value = false;
          return;
        }
        const passengerData = {
          ...passengerForm,
          userId
        };
        
        let result;
        if (isEdit.value) {
          result = await passengerApi.updatePassenger(currentPassenger.value.id, passengerData);
        } else {
          result = await passengerApi.addPassenger(passengerData);
        }
        
        if (result.success) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功');
          
          // 如果是新添加的乘车人，设置currentPassenger和上传配置
          if (!isEdit.value && result.data && result.data.id) {
            currentPassenger.value = result.data;
            formUploadAction.value = `http://localhost:8080/api/passengers/${result.data.id}/id-card-photo`;
            formUploadData.value = {};
            // 不关闭对话框，让用户可以继续上传照片
            ElMessage.info('乘车人添加成功，现在可以上传身份证照片了');
          } else {
            dialogVisible.value = false;
          }
          
          loadPassengerList();
        } else {
          ElMessage.error(result.message || '操作失败');
        }
      } catch (error) {
        ElMessage.error(error.message || '操作失败');
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 删除乘车人
const deletePassenger = async (passengerId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个乘车人吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const result = await passengerApi.deletePassenger(passengerId);
    if (result.success) {
      ElMessage.success('删除成功');
      loadPassengerList();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 设置默认乘车人
const setDefault = async (passengerId) => {
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      return;
    }
    const result = await passengerApi.setDefaultPassenger(userId, passengerId);
    if (result.success) {
      ElMessage.success('设置成功');
      loadPassengerList();
    } else {
      ElMessage.error(result.message || '设置失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '设置失败');
  }
};

// 上传照片
const uploadPhoto = (passenger) => {
  currentPassenger.value = passenger;
  uploadAction.value = `http://localhost:8080/api/passengers/${passenger.id}/id-card-photo`;
  uploadData.value = {};
  photoDialogVisible.value = true;
};

// 查看照片
const viewPhoto = (photoPath) => {
  // photoPath 已经是完整的相对路径，如 /uploads/id-cards/2024-01/xxx.jpg
  currentPhotoUrl.value = `http://localhost:8080${photoPath}`;
  viewPhotoDialogVisible.value = true;
};

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.success) {
    ElMessage.success('上传成功');
    photoDialogVisible.value = false;
    loadPassengerList();
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败');
};

// 表单上传成功
const handleFormUploadSuccess = (response) => {
  if (response.success) {
    ElMessage.success('照片上传成功');
    // 更新表单中的照片路径
    passengerForm.idCardPhotoPath = response.data.filePath;
    // 重新加载列表以更新显示
    loadPassengerList();
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 表单上传失败
const handleFormUploadError = () => {
  ElMessage.error('照片上传失败');
};

// 获取照片URL
const getPhotoUrl = (photoPath) => {
  if (!photoPath) return '';
  return `http://localhost:8080${photoPath}`;
};

// 删除照片
const removePhoto = () => {
  passengerForm.idCardPhotoPath = '';
  // 这里可以添加调用后端删除照片的逻辑
};

// 重置表单
const resetForm = () => {
  if (passengerFormRef.value) {
    passengerFormRef.value.resetFields();
  }
  Object.assign(passengerForm, {
    name: '',
    idCard: '',
    phone: '',
    isDefault: false,
    idCardPhotoPath: ''
  });
  currentPassenger.value = null;
  // 重置上传配置
  formUploadAction.value = '';
  formUploadData.value = {};
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

// 上传身份证照片
const uploadIdCard = async (file) => {
  const userId = getCurrentUserId();
  if (!userId) {
    return;
  }
  
  // 检查是否有当前编辑的乘车人ID
  if (!currentPassenger.value || !currentPassenger.value.id) {
    ElMessage.error('请先选择要上传身份证的乘车人');
    return;
  }
  
  const formData = new FormData();
  formData.append('file', file.raw);
  
  try {
    const result = await passengerApi.uploadIdCardPhoto(currentPassenger.value.id, formData);
    if (result.success) {
      // 自动填充识别的信息
      const { name, idCard } = result.data;
      passengerForm.name = name;
      passengerForm.idCard = idCard;
      ElMessage.success('身份证识别成功');
    } else {
      ElMessage.error(result.message || '身份证识别失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '身份证识别失败');
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadPassengerList();
});
</script>

<style scoped>
.passenger-management {
  padding: 20px;
}

.page-card {
  margin: 0 auto;
  max-width: 1200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #409EFF;
  font-size: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.photo-viewer {
  text-align: center;
  padding: 20px;
}

.el-upload {
  width: 100%;
}

.el-upload__tip {
  margin-top: 10px;
  color: #606266;
  font-size: 12px;
}

.photo-preview {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.uploaded-photo {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.uploaded-photo .photo-preview {
  max-width: 150px;
  max-height: 150px;
  object-fit: cover;
}
</style>