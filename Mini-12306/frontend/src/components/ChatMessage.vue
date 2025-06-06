<template>
  <div class="chat-message" :class="messageClass">
    <!-- 用户消息 -->
    <div v-if="message.sender === 'user'" class="user-message">
      <div class="message-content">
        <div class="message-text">{{ message.content }}</div>
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
      <div class="message-avatar">
        <el-icon>
          <Avatar />
        </el-icon>
      </div>
    </div>

    <!-- AI助手消息 -->
    <div v-else-if="message.sender === 'assistant'" class="assistant-message">
      <div class="message-avatar">
        <el-icon>
          <Avatar />
        </el-icon>
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
          <el-collapse v-model="activeCollapse" class="component-collapse">
            <el-collapse-item name="component" :title="getComponentTitleByName(message.componentType)">
              <component
                :is="getComponentByName(message.componentType)"
                :data="message.componentData"
              />
            </el-collapse-item>
          </el-collapse>
        </div>
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
    </div>

    <!-- 系统消息 -->
    <div v-else class="system-message">
      <div class="system-content">
        <el-icon>
          <InfoFilled />
        </el-icon>
        <span>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { marked } from 'marked'
import { ElIcon, ElCollapse, ElCollapseItem } from 'element-plus'
import { InfoFilled, Avatar } from '@element-plus/icons-vue'

import TicketTableComponent from '@/components/TicketTableComponent.vue'
import PassengerTableComponent from './PassengerTableComponent.vue'
import UserTicketTableComponent from './UserTicketTableComponent.vue'
import CreatePassengerComponent from './CreatePassengerComponent.vue'
import NotificationComponent from './NotificationComponent.vue'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['component-action'])

// 默认展开的折叠面板
const activeCollapse = ref(['component'])

// 计算消息样式类
const messageClass = computed(() => {
  return {
    'user-msg': props.message.sender === 'user',
    'assistant-msg': props.message.sender === 'assistant',
    'system-msg': props.message.sender === 'system',
    streaming: props.message.isStreaming,
  }
})

const getComponentByName = (componentType) => {
  const componentMap = {
    passenger_list: PassengerTableComponent,
    ticket_list: TicketTableComponent,
    user_tickets: UserTicketTableComponent,
    create_passenger: CreatePassengerComponent,
    notification: NotificationComponent,
  }
  return componentMap[componentType] || 'div'
}

// 获取组件标题
const getComponentTitleByName = (componentType) => {
  const titleMap = {
    user_tickets: '我的车票',
    passenger_list: '乘车人',
    ticket_list: '查询车票',
    create_passenger: '创建乘车人',
    notification: '通知',
  }
  return titleMap[componentType] || '组件'
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 如果是今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.getDate() === yesterday.getDate()) {
    return (
      '昨天 ' +
      date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    )
  }

  // 其他日期
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 格式化Markdown
const formatMarkdown = (content) => {
  if (!content) return ''
  return marked(content)
}


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
  max-width: 100%;
  min-width: 100px;
}

.user-message .message-content {
  background: #ffffff;
  color: #333333;
  padding: 12px 16px;
  border-radius: 18px 18px 4px 18px;
  word-wrap: break-word;
}

.assistant-message .message-content {
  background: #ffffff;
  color: #333333;
  padding: 12px 16px;
  border-radius: 18px 18px 18px 4px;
  word-wrap: break-word;
  border: 1px solid #cccccc;
}

.message-text {
  line-height: 1.5;
}

.message-component {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin: 8px 0;
}

.component-collapse {
  border: none;
}

.component-collapse :deep(.el-collapse-item__header) {
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;
  font-weight: 500;
}

.component-collapse :deep(.el-collapse-item__content) {
  padding: 16px;
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
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
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
  background: rgba(0, 0, 0, 0.9);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}
</style>
