<template>
  <div class="user-edit-form">
    <div class="form-header">
      <h3>编辑个人信息</h3>
      <p>请修改您需要更新的信息</p>
    </div>
    
    <el-form 
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      class="edit-form"
    >
      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="formData.username"
          placeholder="请输入用户名"
          :disabled="loading"
        />
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input 
          v-model="formData.phone"
          placeholder="请输入手机号"
          :disabled="loading"
        />
      </el-form-item>
      
      <el-form-item label="邮箱" prop="email">
        <el-input 
          v-model="formData.email"
          placeholder="请输入邮箱地址"
          :disabled="loading"
        />
      </el-form-item>
    </el-form>
    
    <div class="form-actions">
      <el-button @click="handleCancel" :disabled="loading">
        取消
      </el-button>
      <el-button 
        type="primary"
        @click="handleSubmit"
        :loading="loading"
      >
        保存修改
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus';
import { userAPI } from '../../api/user.js';

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

const formRef = ref();
const loading = ref(false);

const formData = reactive({
  id: null,
  username: '',
  phone: '',
  email: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;
    
    loading.value = true;
    
    const response = await userAPI.updateUserInfo(formData);
    if (response.success) {
      ElMessage.success('个人信息更新成功');
      emit('action', {
        type: 'user_info_updated',
        data: formData
      });
    } else {
      ElMessage.error(response.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error('更新失败，请重试');
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  emit('action', {
    type: 'form_cancelled'
  });
};

onMounted(() => {
  if (props.data) {
    Object.assign(formData, props.data);
  }
});
</script>

<style scoped>
.user-edit-form {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin: 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-header {
  margin-bottom: 20px;
  text-align: center;
}

.form-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.form-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.edit-form {
  max-width: 400px;
  margin: 0 auto;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .user-edit-form {
    margin: 8px 0;
    padding: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style>