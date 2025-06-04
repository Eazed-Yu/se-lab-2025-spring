<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>{{ isLogin ? '登录' : '注册' }} Mini-12306</h2>
        </div>
      </template>
      
      <!-- 登录表单 -->
      <el-form
        v-if="isLogin"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <span>还没有账号？</span>
          <el-button type="text" @click="switchForm(false)">立即注册</el-button>
        </div>
      </el-form>
      
      <!-- 注册表单 -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="乘车人姓名" prop="passengerName">
          <el-input v-model="registerForm.passengerName" placeholder="请输入乘车人姓名" />
        </el-form-item>
        
        <el-form-item label="乘车人身份证" prop="passengerIdCard">
          <el-input v-model="registerForm.passengerIdCard" placeholder="请输入乘车人身份证号" />
        </el-form-item>
        
        <el-form-item label="乘车人手机号" prop="passengerPhone">
          <el-input v-model="registerForm.passengerPhone" placeholder="请输入乘车人手机号" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <span>已有账号？</span>
          <el-button type="text" @click="switchForm(true)">返回登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userApi } from '../api';
const router = useRouter();
const isLogin = ref(true);
const loading = ref(false);

// 登录表单
const loginFormRef = ref();
const loginForm = reactive({
  username: '',
  password: ''
});

// 注册表单
const registerFormRef = ref();
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  passengerName: '',
  passengerIdCard: '',
  passengerPhone: '',
  phone: '',
  email: ''
});

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
};

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  passengerName: [
    { required: true, message: '请输入乘车人姓名', trigger: 'blur' }
  ],
  passengerIdCard: [
    { required: true, message: '请输入乘车人身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  passengerPhone: [
    { required: true, message: '请输入乘车人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

// 切换登录/注册表单
const switchForm = (login) => {
  isLogin.value = login;
};

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const result = await userApi.login(loginForm);
        
        if (result.data) {
          localStorage.setItem('user', JSON.stringify(result.data));
          ElMessage.success('登录成功');
          router.push('/tickets/search');
        } else {
          ElMessage.error('登录失败，返回数据格式不正确');
          console.error('登录返回数据格式不正确:', result);
        }
      } catch (error) {
        console.error('登录错误:', error);
        ElMessage.error(error.message || '登录失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const { confirmPassword, ...registerData } = registerForm;
        
        const result = await userApi.register(registerData);
        
        if (result.code === 200) {
          ElMessage.success('注册成功，请登录');
          switchForm(true);
          loginForm.username = registerForm.username;
        } else {
          ElMessage.error(result.message || '注册失败，返回数据格式不正确');
          console.error('注册返回数据格式不正确:', result);
        }
      } catch (error) {
        console.error('注册错误:', error);
        ElMessage.error(error.message || '注册失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 120px);
  background-color: #f5f7fa;
}

.login-card {
  width: 480px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #409EFF;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  color: #606266;
}
</style>