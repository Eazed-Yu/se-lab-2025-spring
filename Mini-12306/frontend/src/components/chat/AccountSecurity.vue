<template>
  <div class="account-security">
    <div class="security-header">
      <h3>账户安全</h3>
      <p>管理您的账户安全设置</p>
    </div>
    
    <div class="security-items">
      <!-- 手机号验证 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <el-icon size="20" color="#409eff"><Phone /></el-icon>
          </div>
          <div class="item-content">
            <h4>手机号验证</h4>
            <p>{{ phoneStatus.text }}</p>
          </div>
        </div>
        <div class="item-status">
          <el-tag :type="phoneStatus.type" size="small">
            {{ phoneStatus.status }}
          </el-tag>
        </div>
        <div class="item-action">
          <el-button 
            size="small"
            :type="phoneStatus.verified ? 'default' : 'primary'"
            @click="handlePhoneAction"
          >
            {{ phoneStatus.actionText }}
          </el-button>
        </div>
      </div>
      
      <!-- 邮箱验证 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <el-icon size="20" color="#409eff"><Message /></el-icon>
          </div>
          <div class="item-content">
            <h4>邮箱验证</h4>
            <p>{{ emailStatus.text }}</p>
          </div>
        </div>
        <div class="item-status">
          <el-tag :type="emailStatus.type" size="small">
            {{ emailStatus.status }}
          </el-tag>
        </div>
        <div class="item-action">
          <el-button 
            size="small"
            :type="emailStatus.verified ? 'default' : 'primary'"
            @click="handleEmailAction"
          >
            {{ emailStatus.actionText }}
          </el-button>
        </div>
      </div>
      
      <!-- 密码安全 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <el-icon size="20" color="#409eff"><Lock /></el-icon>
          </div>
          <div class="item-content">
            <h4>登录密码</h4>
            <p>定期更换密码可以提高账户安全性</p>
          </div>
        </div>
        <div class="item-status">
          <span class="last-update">{{ lastUpdateText }}</span>
        </div>
        <div class="item-action">
          <el-button 
            size="small"
            type="primary"
            @click="handlePasswordChange"
          >
            修改密码
          </el-button>
        </div>
      </div>
      
      <!-- 登录设备 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <el-icon size="20" color="#409eff"><Monitor /></el-icon>
          </div>
          <div class="item-content">
            <h4>登录设备</h4>
            <p>查看和管理您的登录设备</p>
          </div>
        </div>
        <div class="item-status">
          <el-tag type="info" size="small">当前设备</el-tag>
        </div>
        <div class="item-action">
          <el-button 
            size="small"
            @click="handleDeviceManagement"
          >
            管理设备
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="security-tips">
      <div class="tips-header">
        <el-icon><InfoFilled /></el-icon>
        <span>安全提示</span>
      </div>
      <ul>
        <li>建议开启手机号和邮箱验证，提高账户安全性</li>
        <li>定期更换密码，使用强密码</li>
        <li>不要在公共设备上保存登录状态</li>
        <li>发现异常登录及时修改密码</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElIcon, ElTag, ElButton, ElMessage } from 'element-plus';
import { Phone, Message, Lock, Monitor, InfoFilled } from '@element-plus/icons-vue';

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

const securityData = ref({
  userId: null,
  phoneVerified: false,
  emailVerified: false,
  lastUpdateTime: null
});

const phoneStatus = computed(() => {
  const verified = securityData.value.phoneVerified;
  return {
    verified,
    type: verified ? 'success' : 'warning',
    status: verified ? '已验证' : '未验证',
    text: verified ? '您的手机号已通过验证' : '建议验证手机号以提高安全性',
    actionText: verified ? '更换手机' : '立即验证'
  };
});

const emailStatus = computed(() => {
  const verified = securityData.value.emailVerified;
  return {
    verified,
    type: verified ? 'success' : 'warning',
    status: verified ? '已验证' : '未验证',
    text: verified ? '您的邮箱已通过验证' : '建议验证邮箱以提高安全性',
    actionText: verified ? '更换邮箱' : '立即验证'
  };
});

const lastUpdateText = computed(() => {
  if (!securityData.value.lastUpdateTime) {
    return '从未修改';
  }
  
  const updateTime = new Date(securityData.value.lastUpdateTime);
  const now = new Date();
  const diff = now - updateTime;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days === 0) {
    return '今天修改';
  } else if (days < 30) {
    return `${days}天前修改`;
  } else if (days < 365) {
    const months = Math.floor(days / 30);
    return `${months}个月前修改`;
  } else {
    const years = Math.floor(days / 365);
    return `${years}年前修改`;
  }
});

const handlePhoneAction = () => {
  emit('action', {
    type: 'phone_verification',
    data: {
      userId: securityData.value.userId,
      isVerified: securityData.value.phoneVerified
    }
  });
};

const handleEmailAction = () => {
  emit('action', {
    type: 'email_verification',
    data: {
      userId: securityData.value.userId,
      isVerified: securityData.value.emailVerified
    }
  });
};

const handlePasswordChange = () => {
  emit('action', {
    type: 'password_change',
    data: {
      userId: securityData.value.userId
    }
  });
};

const handleDeviceManagement = () => {
  emit('action', {
    type: 'device_management',
    data: {
      userId: securityData.value.userId
    }
  });
};

onMounted(() => {
  if (props.data) {
    Object.assign(securityData.value, props.data);
  }
});
</script>

<style scoped>
.account-security {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin: 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.security-header {
  margin-bottom: 24px;
  text-align: center;
}

.security-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.security-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.security-item:hover {
  border-color: #409eff;
}

.item-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.item-icon {
  margin-right: 12px;
}

.item-content h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.item-content p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.item-status {
  margin-right: 16px;
}

.last-update {
  color: #909399;
  font-size: 12px;
}

.item-action {
  flex-shrink: 0;
}

.security-tips {
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}

.tips-header .el-icon {
  margin-right: 6px;
}

.security-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
}

.security-tips li {
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .account-security {
    margin: 8px 0;
    padding: 16px;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .item-info {
    width: 100%;
  }
  
  .item-status,
  .item-action {
    width: 100%;
    margin: 0;
  }
  
  .item-action .el-button {
    width: 100%;
  }
}
</style>