<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h3>用户注册</h3>
        </div>
      </template>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules" 
        label-width="100px" 
        class="register-form"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-divider content-position="left">默认乘车人信息</el-divider>
        
        <el-form-item label="乘车人姓名" prop="passengerName">
          <el-input 
            v-model="registerForm.passengerName" 
            placeholder="请输入乘车人姓名" 
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="乘车人身份证" prop="passengerIdCard">
          <el-input 
            v-model="registerForm.passengerIdCard" 
            placeholder="请输入乘车人身份证号" 
            prefix-icon="Document"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="乘车人手机" prop="passengerPhone">
          <el-input 
            v-model="registerForm.passengerPhone" 
            placeholder="请输入乘车人手机号" 
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入手机号" 
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="请输入邮箱" 
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading" class="submit-btn">
            注册
          </el-button>
          <el-button @click="resetForm" :disabled="loading" class="reset-btn">
            重置
          </el-button>
        </el-form-item>
        
        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </el-card>
    
    <!-- 注册成功对话框 -->
    <el-dialog v-model="successDialogVisible" title="注册成功" width="30%" center>
      <el-result icon="success" title="注册成功" sub-title="您的账号已成功创建">
        <template #extra>
          <el-button type="primary" @click="goToLogin">前往登录</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Document, Phone, Message } from '@element-plus/icons-vue';
import { userApi } from '../api';
const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);
const successDialogVisible = ref(false);

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  passengerName: '', // 默认乘车人姓名
  passengerIdCard: '', // 默认乘车人身份证
  passengerPhone: '', // 默认乘车人手机号
  phone: '',
  email: ''
});

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'));
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const validateIdCard = (rule, value, callback) => {
  if (value && !/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的18位身份证号'));
  } else {
    callback();
  }
};

const validatePhone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入手机号'));
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'));
  } else {
    callback();
  }
};

const validateEmail = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入邮箱'));
  } else if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'));
  } else {
    callback();
  }
};

// 乘车人手机号验证（可以为空）
const validatePassengerPhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'));
  } else {
    callback();
  }
};

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPass, trigger: 'blur' }
  ],
  passengerName: [
    { required: true, message: '请输入乘车人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  passengerIdCard: [
    { required: true, message: '请输入乘车人身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }
  ],
  passengerPhone: [
    { validator: validatePassengerPhone, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
};

// 提交表单
const submitForm = () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const { confirmPassword, ...registerData } = registerForm;
        
        const result = await userApi.register(registerData);
        
        if (result.code === 200) {
          successDialogVisible.value = true;
        } else {
          ElMessage.error(result.message || '注册失败，请稍后重试');
        }
      } catch (error) {
        ElMessage.error(error.message || '注册失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.warning('请正确填写所有必填项');
      return false;
    }
  });
};

// 重置表单
const resetForm = () => {
  registerFormRef.value.resetFields();
};

// 前往登录页
const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 20px;
}

.register-card {
  width: 500px;
  max-width: 100%;
  margin: 0 auto;
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

.register-form {
  margin-top: 20px;
}

.submit-btn {
  width: 120px;
}

.reset-btn {
  margin-left: 20px;
  width: 120px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.login-link a {
  color: #409EFF;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>