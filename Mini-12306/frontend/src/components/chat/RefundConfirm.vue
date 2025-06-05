<template>
  <div class="refund-confirm">
    <div class="confirm-header">
      <el-icon class="warning-icon"><WarningFilled /></el-icon>
      <h3>退票确认</h3>
      <p class="tip">请仔细核对退票信息，确认后将无法撤销</p>
    </div>
    
    <!-- 票务信息 -->
    <div class="ticket-info">
      <h4>退票信息</h4>
      <div class="ticket-card">
        <div class="train-info">
          <div class="train-number">{{ ticketInfo.trainNumber }}</div>
          <div class="train-type">{{ getTrainType(ticketInfo.trainNumber) }}</div>
        </div>
        
        <div class="route-info">
          <div class="station">
            <div class="time">{{ ticketInfo.departureTime }}</div>
            <div class="name">{{ ticketInfo.departureStation }}</div>
            <div class="date">{{ formatDate(ticketInfo.departureDate) }}</div>
          </div>
          <div class="arrow">
            <el-icon><Right /></el-icon>
          </div>
          <div class="station">
            <div class="time">{{ ticketInfo.arrivalTime }}</div>
            <div class="name">{{ ticketInfo.arrivalStation }}</div>
            <div class="date">{{ formatDate(ticketInfo.arrivalDate) }}</div>
          </div>
        </div>
        
        <div class="seat-info">
          <div class="seat-details">
            <span class="seat-type">{{ ticketInfo.seatType }}</span>
            <span class="seat-number">{{ ticketInfo.seatNumber }}</span>
          </div>
          <div class="passenger-info">
            <span class="passenger-name">{{ ticketInfo.passengerName }}</span>
            <span class="id-number">{{ maskIdNumber(ticketInfo.idNumber) }}</span>
          </div>
        </div>
        
        <div class="price-info">
          <div class="original-price">¥{{ ticketInfo.price }}</div>
        </div>
      </div>
    </div>
    
    <!-- 退票原因 -->
    <div class="refund-reason">
      <h4>退票原因</h4>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="退票原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择退票原因">
            <el-option label="行程取消" value="TRIP_CANCELLED" />
            <el-option label="行程变更" value="SCHEDULE_CHANGE" />
            <el-option label="个人原因" value="PERSONAL_REASON" />
            <el-option label="身体不适" value="HEALTH_ISSUE" />
            <el-option label="工作安排" value="WORK_ARRANGEMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="form.reason === 'OTHER'" label="详细说明" prop="reasonDetail">
          <el-input 
            v-model="form.reasonDetail" 
            type="textarea" 
            placeholder="请详细说明退票原因"
            :rows="3"
          />
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 退款信息 -->
    <div class="refund-info">
      <h4>退款信息</h4>
      <div class="refund-details">
        <div class="refund-item">
          <span class="label">票面价格</span>
          <span class="value">¥{{ ticketInfo.price }}</span>
        </div>
        <div class="refund-item">
          <span class="label">退票手续费</span>
          <span class="value fee">-¥{{ refundFee }}</span>
        </div>
        <div class="refund-item total">
          <span class="label">实际退款</span>
          <span class="value">¥{{ actualRefund }}</span>
        </div>
      </div>
      
      <div class="refund-rules">
        <h5>退票规则说明</h5>
        <ul>
          <li>开车前15天（不含）以上退票，不收取退票费</li>
          <li>开车前48小时以上的，收取票价5%的退票费</li>
          <li>开车前24小时以上、不足48小时的，收取票价10%的退票费</li>
          <li>开车前不足24小时的，收取票价20%的退票费</li>
        </ul>
      </div>
      
      <div class="refund-method">
        <h5>退款方式</h5>
        <div class="method-info">
          <el-icon><CreditCard /></el-icon>
          <span>原支付方式退回</span>
          <span class="time-info">（预计1-7个工作日到账）</span>
        </div>
      </div>
    </div>
    
    <!-- 重要提醒 -->
    <div class="important-notice">
      <h4>
        <el-icon><InfoFilled /></el-icon>
        重要提醒
      </h4>
      <ul>
        <li>退票成功后，车票将立即失效，无法恢复</li>
        <li>如需重新购票，请重新查询并购买</li>
        <li>退款将按原支付方式返回，请注意查收</li>
        <li>特殊情况下退款可能延迟，请耐心等待</li>
      </ul>
    </div>
    
    <!-- 确认操作 -->
    <div class="confirm-actions">
      <el-checkbox v-model="agreed" class="agreement">
        我已阅读并同意退票规则，确认申请退票
      </el-checkbox>
      
      <div class="actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="danger" 
          @click="handleConfirm" 
          :disabled="!canConfirm"
          :loading="submitting"
        >
          {{ submitting ? '处理中...' : '确认退票' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { Right, WarningFilled, InfoFilled, CreditCard } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({
  ticketInfo: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

const formRef = ref();
const agreed = ref(false);
const submitting = ref(false);

const form = reactive({
  reason: '',
  reasonDetail: ''
});

const rules = {
  reason: [
    { required: true, message: '请选择退票原因', trigger: 'change' }
  ],
  reasonDetail: [
    { required: true, message: '请详细说明退票原因', trigger: 'blur' }
  ]
};

// 计算退票手续费
const refundFee = computed(() => {
  const now = new Date();
  const departureTime = new Date(props.ticketInfo.departureDate + ' ' + props.ticketInfo.departureTime);
  const hoursUntilDeparture = (departureTime.getTime() - now.getTime()) / (1000 * 60 * 60);
  
  const price = parseFloat(props.ticketInfo.price);
  
  if (hoursUntilDeparture >= 15 * 24) {
    return 0; // 15天以上免费
  } else if (hoursUntilDeparture >= 48) {
    return Math.round(price * 0.05); // 5%手续费
  } else if (hoursUntilDeparture >= 24) {
    return Math.round(price * 0.1); // 10%手续费
  } else {
    return Math.round(price * 0.2); // 20%手续费
  }
});

// 实际退款金额
const actualRefund = computed(() => {
  return parseFloat(props.ticketInfo.price) - refundFee.value;
});

// 是否可以确认退票
const canConfirm = computed(() => {
  return agreed.value && 
         form.reason && 
         (form.reason !== 'OTHER' || form.reasonDetail);
});

const getTrainType = (trainNumber) => {
  if (trainNumber.startsWith('G')) return '高速';
  if (trainNumber.startsWith('D')) return '动车';
  if (trainNumber.startsWith('C')) return '城际';
  if (trainNumber.startsWith('K')) return '快速';
  if (trainNumber.startsWith('T')) return '特快';
  return '普通';
};

const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

const maskIdNumber = (idNumber) => {
  if (!idNumber) return '';
  if (idNumber.length <= 8) return idNumber;
  return idNumber.substring(0, 4) + '****' + idNumber.substring(idNumber.length - 4);
};

const handleConfirm = async () => {
  try {
    await formRef.value.validate();
    
    await ElMessageBox.confirm(
      `确认退票后将扣除手续费¥${refundFee.value}，实际退款¥${actualRefund.value}，是否继续？`,
      '最终确认',
      {
        confirmButtonText: '确认退票',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    );
    
    submitting.value = true;
    
    const refundData = {
      ticketInfo: props.ticketInfo,
      reason: form.reason,
      reasonDetail: form.reasonDetail,
      refundFee: refundFee.value,
      actualRefund: actualRefund.value
    };
    
    emit('action', {
      type: 'confirm_refund',
      data: refundData
    });
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退票确认失败:', error);
    }
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  emit('action', {
    type: 'cancel_refund'
  });
};
</script>

<style scoped>
.refund-confirm {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.confirm-header {
  text-align: center;
  margin-bottom: 30px;
}

.warning-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 12px;
}

.confirm-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.tip {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.ticket-info,
.refund-reason,
.refund-info,
.important-notice {
  margin-bottom: 30px;
}

.ticket-info h4,
.refund-reason h4,
.refund-info h4,
.important-notice h4 {
  margin: 0 0 16px 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ticket-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 20px;
  align-items: center;
}

.train-info {
  text-align: center;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.train-type {
  font-size: 12px;
  color: #909399;
}

.route-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.station {
  text-align: center;
}

.station .time {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.station .name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 2px;
}

.station .date {
  font-size: 12px;
  color: #909399;
}

.arrow {
  color: #909399;
}

.seat-info {
  text-align: center;
}

.seat-details {
  margin-bottom: 8px;
}

.seat-type {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.seat-number {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.passenger-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.passenger-name {
  font-size: 14px;
  color: #303133;
}

.id-number {
  font-size: 12px;
  color: #909399;
}

.price-info {
  text-align: center;
}

.original-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.refund-details {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.refund-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.refund-item:last-child {
  margin-bottom: 0;
}

.refund-item.total {
  padding-top: 8px;
  border-top: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: bold;
}

.refund-item .label {
  color: #606266;
}

.refund-item .value {
  color: #303133;
  font-weight: 500;
}

.refund-item .fee {
  color: #f56c6c;
}

.refund-rules,
.refund-method {
  margin-bottom: 20px;
}

.refund-rules h5,
.refund-method h5 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.refund-rules ul {
  margin: 0;
  padding-left: 20px;
}

.refund-rules li {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 4px;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.time-info {
  color: #909399;
  font-size: 12px;
}

.important-notice {
  padding: 16px;
  background: #fff7e6;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.important-notice ul {
  margin: 0;
  padding-left: 20px;
}

.important-notice li {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 4px;
}

.confirm-actions {
  text-align: center;
}

.agreement {
  margin-bottom: 20px;
  color: #606266;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .ticket-card {
    grid-template-columns: 1fr;
    gap: 16px;
    text-align: center;
  }
  
  .route-info {
    justify-content: center;
  }
  
  .refund-item {
    font-size: 14px;
  }
}
</style>