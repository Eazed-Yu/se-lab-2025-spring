<template>
  <div class="notification-component">
    <div class="notification-header">
      <el-icon class="notification-icon" :class="getIconClass()">
        <component :is="getIconComponent()" />
      </el-icon>
      <h3 class="notification-title">{{ data.title || '通知' }}</h3>
    </div>
    
    <div class="notification-content">
      <div class="notification-message">
        {{ data.message }}
      </div>
      
      <!-- 订单详情 -->
      <div v-if="data.orderInfo" class="order-details">
        <div class="detail-row">
          <span class="label">订单号：</span>
          <span class="value">{{ data.orderInfo.orderNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="label">车次：</span>
          <span class="value">{{ data.orderInfo.trainNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="label">出发站：</span>
          <span class="value">{{ data.orderInfo.departureStation }}</span>
        </div>
        <div class="detail-row">
          <span class="label">到达站：</span>
          <span class="value">{{ data.orderInfo.arrivalStation }}</span>
        </div>
        <div class="detail-row">
          <span class="label">出发时间：</span>
          <span class="value">{{ data.orderInfo.departureTime }}</span>
        </div>
        <div class="detail-row">
          <span class="label">座位类型：</span>
          <span class="value">{{ data.orderInfo.seatType }}</span>
        </div>
        <div class="detail-row">
          <span class="label">乘车人：</span>
          <span class="value">{{ data.orderInfo.passengerName }}</span>
        </div>
        <div class="detail-row" v-if="data.orderInfo.seatNumber">
          <span class="label">座位号：</span>
          <span class="value">{{ data.orderInfo.seatNumber }}</span>
        </div>
        <div class="detail-row" v-if="data.orderInfo.price">
          <span class="label">票价：</span>
          <span class="value price">¥{{ data.orderInfo.price }}</span>
        </div>
        <div class="detail-row" v-if="data.orderInfo.ticketCount && data.orderInfo.ticketCount > 1">
          <span class="label">票数：</span>
          <span class="value">{{ data.orderInfo.ticketCount }}张</span>
        </div>
        <div class="detail-row" v-if="data.orderInfo.totalAmount">
          <span class="label">总金额：</span>
          <span class="value price">¥{{ data.orderInfo.totalAmount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElIcon } from 'element-plus'
import { 
  CircleCheck, 
  Warning, 
  CircleClose, 
  InfoFilled 
} from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({})
  }
})


// 根据通知类型获取图标组件
const getIconComponent = () => {
  const iconMap = {
    success: CircleCheck,
    warning: Warning,
    error: CircleClose,
    info: InfoFilled
  }
  return iconMap[props.data.type] || InfoFilled
}

// 根据通知类型获取图标样式类
const getIconClass = () => {
  return {
    'success-icon': props.data.type === 'success',
    'warning-icon': props.data.type === 'warning',
    'error-icon': props.data.type === 'error',
    'info-icon': props.data.type === 'info'
  }
}

</script>

<style scoped>
.notification-component {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.notification-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.notification-icon {
  font-size: 20px;
  margin-right: 8px;
}

.success-icon {
  color: #67c23a;
}

.warning-icon {
  color: #e6a23c;
}

.error-icon {
  color: #f56c6c;
}

.info-icon {
  color: #409eff;
}

.notification-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.notification-content {
  padding: 16px;
}

.notification-message {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 16px;
}

.order-details {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
  font-weight: 500;
}

.value {
  color: #303133;
  font-weight: 400;
}

.price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.notification-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>