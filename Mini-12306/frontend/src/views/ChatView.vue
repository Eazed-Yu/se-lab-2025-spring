<template>
  <div class="chat-view">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <h3 v-if="!sidebarCollapsed">对话历史</h3>
        <el-button 
          :icon="sidebarCollapsed ? Expand : Fold"
          @click="toggleSidebar"
          circle
          size="small"
        />
      </div>
      
      <div v-if="!sidebarCollapsed" class="sidebar-content">
        <el-button 
          type="primary" 
          @click="createNewSession"
          class="new-chat-btn"
          :loading="creatingSession"
        >
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
        
        <div class="session-list">
          <div 
            v-for="session in sessions"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: currentSessionId === session.sessionId }"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-title">{{ session.title }}</div>
            <div class="session-time">{{ formatSessionTime(session.lastUpdateTime) }}</div>
            <div class="session-actions">
              <el-button 
                :icon="Edit"
                @click.stop="editSessionTitle(session)"
                circle
                size="small"
                text
              />
              <el-button 
                :icon="Delete"
                @click.stop="deleteSession(session.sessionId)"
                circle
                size="small"
                text
                type="danger"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主聊天区域 -->
    <div class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-title">
          <h3>{{ currentSession?.title || '智能助手' }}</h3>
          <div class="connection-status">
            <el-tag 
              :type="connectionStatus.type"
              size="small"
            >
              {{ connectionStatus.text }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-if="!currentSession" class="welcome-screen">
          <div class="welcome-content">
            <el-icon size="64" color="#409eff"><ChatDotRound /></el-icon>
            <h2>欢迎使用智能助手</h2>
            <p>我可以帮您查询车票、购票、改签、退票以及管理乘车人信息</p>
            <div class="quick-actions">
              <el-button @click="sendQuickMessage('查询车票')">查询车票</el-button>
              <el-button @click="sendQuickMessage('管理乘车人')">管理乘车人</el-button>
              <el-button @click="sendQuickMessage('我的订单')">我的订单</el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="messages">
          <ChatMessage 
            v-for="message in currentSession.messages"
            :key="message.messageId"
            :message="message"
            @component-action="handleComponentAction"
          />
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input 
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题..."
            @keydown.enter.exact="handleEnterKey"
            @keydown.enter.shift.exact.prevent="addNewLine"
            :disabled="!currentUser || sending"
            class="message-input"
          />
          <div class="input-actions">
            <el-button 
              type="primary"
              @click="sendMessage"
              :loading="sending"
              :disabled="!canSend"
            >
              发送
            </el-button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElButton, ElIcon, ElTag, ElInput, ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Expand, Fold, ChatDotRound } from '@element-plus/icons-vue';
import ChatMessage from '../components/chat/ChatMessage.vue';
import { chatWebSocket } from '../api/websocket.js';
import { chatAPI } from '../api/chat.js';

const router = useRouter();

// 响应式数据
const sidebarCollapsed = ref(false);
const sessions = ref([]);
const currentSessionId = ref(null);
const inputMessage = ref('');
const sending = ref(false);
const creatingSession = ref(false);
const messageListRef = ref();

// 当前用户信息
const currentUser = ref(null);

// WebSocket连接状态
const { isConnected, isConnecting } = chatWebSocket.getConnectionState();

// 计算属性
const currentSession = computed(() => {
  return sessions.value.find(s => s.sessionId === currentSessionId.value);
});

const connectionStatus = computed(() => {
  if (isConnected.value) {
    return { type: 'success', text: '已连接' };
  } else if (isConnecting.value) {
    return { type: 'warning', text: '连接中...' };
  } else {
    return { type: 'danger', text: '未连接' };
  }
});

const canSend = computed(() => {
  return inputMessage.value.trim() && !sending.value && currentUser.value;
});

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

const createNewSession = async () => {
  if (!currentUser.value) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    creatingSession.value = true;
    const response = await chatAPI.createSession(currentUser.value.id);
    
    if (response.success) {
      const newSession = response.data;
      sessions.value.unshift(newSession);
      await switchSession(newSession.sessionId);
    } else {
      ElMessage.error(response.message || '创建会话失败');
    }
  } catch (error) {
    console.error('创建会话失败:', error);
    ElMessage.error('创建会话失败');
  } finally {
    creatingSession.value = false;
  }
};

const switchSession = async (sessionId) => {
  if (currentSessionId.value === sessionId) return;
  
  try {
    // 断开当前连接
    chatWebSocket.disconnect();
    
    // 切换会话
    currentSessionId.value = sessionId;
    
    // 重新连接WebSocket
    await chatWebSocket.connect(currentUser.value.id, sessionId);
    
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('切换会话失败:', error);
    ElMessage.error('切换会话失败');
  }
};

const editSessionTitle = async (session) => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      '请输入新的对话标题',
      '编辑标题',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: session.title,
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '标题不能为空';
          }
          return true;
        }
      }
    );
    
    const response = await chatAPI.updateSessionTitle(session.sessionId, newTitle.trim());
    if (response.success) {
      session.title = newTitle.trim();
      ElMessage.success('标题更新成功');
    } else {
      ElMessage.error(response.message || '更新标题失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('编辑标题失败:', error);
      ElMessage.error('编辑标题失败');
    }
  }
};

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个对话吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const response = await chatAPI.deleteSession(sessionId);
    if (response.success) {
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId);
      
      // 如果删除的是当前会话，切换到第一个会话或创建新会话
      if (currentSessionId.value === sessionId) {
        if (sessions.value.length > 0) {
          await switchSession(sessions.value[0].sessionId);
        } else {
          currentSessionId.value = null;
          chatWebSocket.disconnect();
        }
      }
      
      ElMessage.success('对话删除成功');
    } else {
      ElMessage.error(response.message || '删除对话失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error);
      ElMessage.error('删除对话失败');
    }
  }
};

const sendMessage = async () => {
  if (!canSend.value) return;
  
  // 如果没有当前会话，先创建一个
  if (!currentSessionId.value) {
    await createNewSession();
    if (!currentSessionId.value) {
      ElMessage.error('创建会话失败，无法发送消息');
      return;
    }
  }
  
  // 如果没有连接，尝试连接
  if (!isConnected.value) {
    try {
      await chatWebSocket.connect(currentUser.value.id, currentSessionId.value);
    } catch (error) {
      console.error('连接失败:', error);
      ElMessage.error('连接失败，无法发送消息');
      return;
    }
  }
  
  const message = inputMessage.value.trim();
  inputMessage.value = '';
  
  // 立即添加用户消息到当前会话
  const userMessage = {
    messageId: Date.now().toString(),
    sessionId: currentSessionId.value,
    type: 'text',
    sender: 'user',
    content: message,
    status: 'sent',
    timestamp: new Date().toISOString()
  };
  
  if (currentSession.value) {
    currentSession.value.messages.push(userMessage);
    nextTick(() => {
      scrollToBottom();
    });
  }
  
  try {
    sending.value = true;
    await chatWebSocket.sendMessage(message);
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败');
    inputMessage.value = message; // 恢复输入内容
    
    // 发送失败时，移除刚添加的用户消息
    if (currentSession.value) {
      const messageIndex = currentSession.value.messages.findIndex(msg => msg.messageId === userMessage.messageId);
      if (messageIndex > -1) {
        currentSession.value.messages.splice(messageIndex, 1);
      }
    }
  } finally {
    sending.value = false;
  }
};

const sendQuickMessage = async (message) => {
  if (!currentUser.value) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  
  // 如果没有当前会话，先创建一个
  if (!currentSessionId.value) {
    await createNewSession();
  }
  
  inputMessage.value = message;
  await sendMessage();
};

const handleEnterKey = (event) => {
  if (!event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
};

const addNewLine = () => {
  inputMessage.value += '\n';
};

const handleComponentAction = (action) => {
  console.log('组件动作:', action);
  
  switch (action.type) {
    case 'purchase_ticket':
      // 处理购票动作
      break;
    case 'add_passenger':
      // 处理添加乘车人动作
      break;
    // 其他动作...
  }
};

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
};

const formatSessionTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  
  return date.toLocaleDateString('zh-CN');
};

const loadUserSessions = async () => {
  if (!currentUser.value) return;
  
  try {
    const response = await chatAPI.getUserSessions(currentUser.value.id);
    if (response.success) {
      sessions.value = response.data;
    }
  } catch (error) {
    console.error('加载用户会话失败:', error);
  }
};

const initializeChat = async () => {
  // 获取用户信息
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  
  try {
    currentUser.value = JSON.parse(userStr);
  } catch (error) {
    console.error('解析用户信息失败:', error);
    ElMessage.error('用户信息异常，请重新登录');
    router.push('/login');
    return;
  }
  
  // 加载用户会话
  await loadUserSessions();
  
  // 如果有会话，连接到最新的会话
  if (sessions.value.length > 0) {
    await switchSession(sessions.value[0].sessionId);
  }
};

// 监听消息
const handleWebSocketMessage = (message) => {
  if (currentSession.value) {
    currentSession.value.messages.push(message);
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 生命周期
onMounted(async () => {
  await initializeChat();
  
  // 订阅WebSocket消息
  chatWebSocket.onMessage(handleWebSocketMessage);
});

onUnmounted(() => {
  chatWebSocket.disconnect();
});

// 监听当前会话变化，自动滚动到底部
watch(
  () => currentSession.value?.messages?.length,
  () => {
    nextTick(() => {
      scrollToBottom();
    });
  }
);
</script>

<style scoped>
.chat-view {
  display: flex;
  height: 80vh;
  background: #f5f7fa;
}

.sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.sidebar-collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.sidebar-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.new-chat-btn {
  width: 100%;
  margin-bottom: 16px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  padding: 12px;
  padding-right: 64px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #e6f7ff;
  border: 1px solid #409eff;
}

.session-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.session-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: none;
  gap: 4px;
}

.session-item:hover .session-actions {
  display: flex;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.chat-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-title h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.welcome-screen {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.welcome-content {
  text-align: center;
  max-width: 400px;
}

.welcome-content h2 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.welcome-content p {
  color: #606266;
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.messages {
  min-height: 100%;
}

.input-area {
  padding: 16px 24px 0 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .sidebar:not(.sidebar-collapsed) {
    transform: translateX(0);
  }
  
  .chat-main {
    width: 100%;
  }
  
  .input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .quick-actions {
    flex-direction: column;
  }
}
</style>