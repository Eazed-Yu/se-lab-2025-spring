<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const currentUser = ref(null);

// 计算属性：用户是否已登录
const isLoggedIn = computed(() => !!currentUser.value);

// 在组件挂载时从本地存储获取用户信息
onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr);
    } catch (e) {
      console.error('解析用户信息失败', e);
      localStorage.removeItem('user');
    }
  }
});

// 网页url变化时，更新currentUser
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr);
    } catch (e) {
      console.error('解析用户信息失败', e);
      localStorage.removeItem('user');
    }
  } else {
    currentUser.value = null;
  }
  next();
})


// 退出登录
const logout = () => {
  localStorage.removeItem('user');
  currentUser.value = null;
  ElMessage.success('已退出登录');
  router.push('/login');
};
</script>

<template>
  <el-container class="layout-container">
    <el-header class="app-header">
      <div class="logo">Mini-12306</div>
      <el-menu
        class="app-menu"
        :router="true"
        mode="horizontal"
        :ellipsis="false"
        :default-active="$route.path"
        background-color="#409EFF"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <template v-if="!isLoggedIn">
          <el-menu-item index="/login">登录</el-menu-item>
          <el-menu-item index="/register">注册</el-menu-item>
        </template>
        
        <!-- 已登录状态显示功能菜单 -->
        <template v-else>
          <el-menu-item index="/tickets/search">车票查询与购买</el-menu-item>
          <el-menu-item index="/tickets/user">我的车票与退票</el-menu-item>
          <el-menu-item index="/passengers">乘车人管理</el-menu-item>
          <el-menu-item index="/chat">智能助手</el-menu-item>
          
          <!-- 用户信息和退出登录 -->
          <div class="user-info">
            <span>欢迎，{{ currentUser?.username }}</span>
            <el-button type="text" @click="logout" class="logout-btn">退出登录</el-button>
          </div>
        </template>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    <el-footer class="app-footer"> Mini-12306 演示系统 © 2025 </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  padding: 0;
  background-color: #409eff;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.8rem;
  font-weight: bold;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
}
.app-menu {
  flex: 1;
}

.app-footer {
  text-align: center;
  background-color: #f8f8f8;
  color: #606266;
  padding: 20px;
}


:deep(.el-menu--horizontal > .el-menu-item) {
  height: 60px;
  line-height: 60px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-left: auto;
  padding: 0 20px;
  color: #fff;
  height: 60px;
}

.logout-btn {
  color: #fff;
  margin-left: 10px;
}

.logout-btn:hover {
  color: #ffd04b;
}
</style>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>