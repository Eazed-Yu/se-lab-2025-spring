<script setup></script>

<template>
  <el-container class="layout-container">
    <el-header class="app-header">
      <div class="logo">Mini-12306</div>
      <el-menu
        class="app-menu"
        :router="true"
        mode="horizontal"
        :default-active="$route.path"
        background-color="#409EFF"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/tickets/search">车票查询与购买</el-menu-item>
        <el-menu-item index="/tickets/user">我的车票</el-menu-item>
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
  display: flex;
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
  flex-grow: 1;
}

.app-footer {
  text-align: center;
  background-color: #f8f8f8;
  color: #606266;
  padding: 20px;
}

:deep(.el-menu.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu--horizontal > .el-menu-item) {
  height: 60px;
  line-height: 60px;
}
</style>
