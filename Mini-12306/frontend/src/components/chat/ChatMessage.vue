<template>
  <div class="chat-message" :class="messageClass">
    <!-- 用户消息 -->
    <div v-if="message.sender === 'user'" class="user-message">
      <div class="message-content">
        <div class="message-text">{{ message.content }}</div>
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
      <div class="message-avatar">
        <el-avatar :size="32" icon="User" />
      </div>
    </div>

    <!-- AI助手消息 -->
    <div v-else-if="message.sender === 'assistant'" class="assistant-message">
      <div class="message-avatar">
        <el-avatar :size="32" icon="ChatDotRound" />
      </div>
      <div class="message-content">
        <!-- 文本消息 -->
        <div v-if="message.type === 'text'" class="message-text">
          <div v-if="message.isStreaming" class="streaming-text">
            {{ message.content }}
            <span v-if="!message.streamEnd" class="cursor">|</span>
          </div>
          <div v-else v-html="formatMarkdown(message.content)"></div>
        </div>
        
        <!-- 组件消息 -->
        <div v-else-if="message.type === 'component'" class="message-component">
          <component 
            :is="getComponentName(message.componentType)" 
            :data="message.componentData"
            @action="handleComponentAction"
          />
        </div>
        
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
    </div>

    <!-- 系统消息 -->
    <div v-else class="system-message">
      <div class="system-content">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { marked } from 'marked';
import { ElAvatar, ElIcon } from 'element-plus';
import { User, ChatDotRound, InfoFilled } from '@element-plus/icons-vue';

// 动态导入组件
import TrainList from './components/TrainList.vue';
import TicketForm from './components/TicketForm.vue';
import PassengerForm from './PassengerForm.vue';
import PassengerList from './PassengerList.vue';
import IdCardUpload from './IdCardUpload.vue';
import ChangeTicketForm from './ChangeTicketForm.vue';
import RefundConfirm from './RefundConfirm.vue';
import UserEditForm from './UserEditForm.vue';
import PasswordChangeForm from './PasswordChangeForm.vue';
import AccountSecurity from './AccountSecurity.vue';

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['component-action']);

// 计算消息样式类
const messageClass = computed(() => {
  return {
    'user-msg': props.message.sender === 'user',
    'assistant-msg': props.message.sender === 'assistant',
    'system-msg': props.message.sender === 'system',
    'streaming': props.message.isStreaming
  };
});

// 获取组件名称
const getComponentName = (componentType) => {
  const componentMap = {
    'train_list': TrainList,
    'ticket_form': TicketForm,
    'passenger_form': PassengerForm,
    'passenger_list': PassengerList,
    'id_card_upload': IdCardUpload,
    'change_ticket_form': ChangeTicketForm,
    'refund_confirm': RefundConfirm,
    'user_edit_form': UserEditForm,
    'password_change_form': PasswordChangeForm,
    'account_security': AccountSecurity
  };
  
  return componentMap[componentType] || 'div';
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  
  // 如果是今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // 如果是昨天
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.getDate() === yesterday.getDate()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // 其他日期
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 格式化Markdown
const formatMarkdown = (content) => {
  if (!content) return '';
  return marked(content);
};

// 处理组件动作
const handleComponentAction = (action) => {
  emit('component-action', action);
};
</script>

<style scoped>
.chat-message {
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.assistant-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 70%;
  min-width: 100px;
}

.user-message .message-content {
  background: #409eff;
  color: white;
  padding: 12px 16px;
  border-radius: 18px 18px 4px 18px;
  word-wrap: break-word;
}

.assistant-message .message-content {
  background: #f5f7fa;
  color: #303133;
  padding: 12px 16px;
  border-radius: 18px 18px 18px 4px;
  word-wrap: break-word;
}

.message-text {
  line-height: 1.5;
}

.message-component {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin: 8px 0;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.assistant-message .message-time {
  text-align: left;
}

.system-message {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.system-content {
  background: #f0f9ff;
  color: #409eff;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.streaming-text {
  position: relative;
}

.cursor {
  animation: blink 1s infinite;
  color: #409eff;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.message-avatar {
  flex-shrink: 0;
}

/* Markdown样式 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3) {
  margin: 8px 0 4px 0;
  font-weight: bold;
}

.message-text :deep(p) {
  margin: 4px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}
</style>