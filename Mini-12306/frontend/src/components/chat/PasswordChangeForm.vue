<template>
  <div class="password-change-form">
    <div class="form-header">
      <h3>修改密码</h3>
      <p>为了您的账户安全，请定期更换密码</p>
    </div>
    
    <el-form 
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="change-form"
    >
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input 
          v-model="formData.currentPassword"
          type="password"
          placeholder="请输入当前密码"
          :disabled="loading"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="新密码" prop="newPassword">
        <el-input 
          v-model="formData.newPassword"
          type="password"
          placeholder="请输入新密码"
          :disabled="loading"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input 
          v-model="formData.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          :disabled="loading"
          show-password
        />
      </el-form-item>
    </el-form>
    
    <div class="password-tips">
      <h4>密码要求：</h4>
      <ul>
        <li>长度至少8位</li>
        <li>包含大小写字母</li>
        <li>包含数字</li>
        <li>可包含特殊字符</li>
      </ul>
    </div>
    
    <div class="form-actions">
      <el-button @click="handleCancel" :disabled="loading">
        取消
      </el-button>
      <el-button 
        type="primary"
        @click="handleSubmit"
        :loading="loading"
      >
        修改密码
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
  userId: null,
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'));
  } else if (value.length < 8) {
    callback(new Error('密码长度至少8位'));
  } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含大小写字母和数字'));
  } else {
    callback();
  }
};

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'));
  } else if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;
    
    loading.value = true;
    
    const response = await userAPI.changePassword({
      userId: formData.userId,
      currentPassword: formData.currentPassword,
      newPassword: formData.newPassword
    });
    
    if (response.success) {
      ElMessage.success('密码修改成功，请重新登录');
      emit('action', {
        type: 'password_changed',
        data: { requireRelogin: true }
      });
    } else {
      ElMessage.error(response.message || '密码修改失败');
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    ElMessage.error('修改失败，请重试');
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
  if (props.data && props.data.userId) {
    formData.userId = props.data.userId;
  }
});
</script>

<style scoped>
.password-change-form {
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

.change-form {
  max-width: 400px;
  margin: 0 auto;
}

.password-tips {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin: 20px 0;
}

.password-tips h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
}

.password-tips li {
  margin-bottom: 4px;
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
  .password-change-form {
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