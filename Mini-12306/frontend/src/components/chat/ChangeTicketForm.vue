<template>
  <div class="change-ticket-form">
    <div class="form-header">
      <h3>改签申请</h3>
      <p class="tip">请选择要改签的车次和座位</p>
    </div>
    
    <!-- 原票信息 -->
    <div class="original-ticket">
      <h4>原票信息</h4>
      <div class="ticket-info">
        <div class="train-info">
          <span class="train-number">{{ originalTicket.trainNumber }}</span>
          <span class="train-type">{{ getTrainType(originalTicket.trainNumber) }}</span>
        </div>
        <div class="route-info">
          <div class="station">
            <div class="time">{{ originalTicket.departureTime }}</div>
            <div class="name">{{ originalTicket.departureStation }}</div>
          </div>
          <div class="arrow">
            <el-icon><Right /></el-icon>
          </div>
          <div class="station">
            <div class="time">{{ originalTicket.arrivalTime }}</div>
            <div class="name">{{ originalTicket.arrivalStation }}</div>
          </div>
        </div>
        <div class="seat-info">
          <span class="seat-type">{{ originalTicket.seatType }}</span>
          <span class="seat-number">{{ originalTicket.seatNumber }}</span>
          <span class="price">¥{{ originalTicket.price }}</span>
        </div>
      </div>
    </div>
    
    <!-- 改签选择 -->
    <div class="change-selection">
      <h4>改签选择</h4>
      
      <!-- 日期选择 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="出发日期" prop="departureDate">
          <el-date-picker
            v-model="form.departureDate"
            type="date"
            placeholder="选择出发日期"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="车次选择" prop="selectedTrain">
          <div v-if="availableTrains.length === 0" class="no-trains">
            <el-empty description="暂无可改签车次">
              <el-button type="primary" @click="searchTrains">重新搜索</el-button>
            </el-empty>
          </div>
          <div v-else class="train-list">
            <div 
              v-for="train in availableTrains" 
              :key="train.id"
              class="train-item"
              :class="{ selected: form.selectedTrain?.id === train.id }"
              @click="selectTrain(train)"
            >
              <div class="train-basic">
                <div class="train-number">{{ train.trainNumber }}</div>
                <div class="train-route">
                  <span class="time">{{ train.departureTime }}</span>
                  <span class="station">{{ train.departureStation }}</span>
                  <el-icon class="arrow"><Right /></el-icon>
                  <span class="time">{{ train.arrivalTime }}</span>
                  <span class="station">{{ train.arrivalStation }}</span>
                </div>
                <div class="duration">{{ train.duration }}</div>
              </div>
              
              <div class="seat-options">
                <div 
                  v-for="seat in train.availableSeats" 
                  :key="seat.type"
                  class="seat-option"
                  :class="{ 
                    selected: form.selectedSeat?.type === seat.type && form.selectedTrain?.id === train.id,
                    unavailable: seat.count === 0
                  }"
                  @click.stop="selectSeat(train, seat)"
                >
                  <div class="seat-type">{{ seat.type }}</div>
                  <div class="seat-price">¥{{ seat.price }}</div>
                  <div class="seat-count">{{ seat.count > 0 ? `${seat.count}张` : '无票' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
        
        <!-- 改签原因 -->
        <el-form-item label="改签原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择改签原因">
            <el-option label="行程变更" value="SCHEDULE_CHANGE" />
            <el-option label="个人原因" value="PERSONAL_REASON" />
            <el-option label="工作安排" value="WORK_ARRANGEMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="form.reason === 'OTHER'" label="详细说明" prop="reasonDetail">
          <el-input 
            v-model="form.reasonDetail" 
            type="textarea" 
            placeholder="请详细说明改签原因"
            :rows="3"
          />
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 费用说明 -->
    <div v-if="form.selectedTrain && form.selectedSeat" class="fee-info">
      <h4>费用说明</h4>
      <div class="fee-details">
        <div class="fee-item">
          <span>原票价</span>
          <span>¥{{ originalTicket.price }}</span>
        </div>
        <div class="fee-item">
          <span>新票价</span>
          <span>¥{{ form.selectedSeat.price }}</span>
        </div>
        <div class="fee-item">
          <span>改签手续费</span>
          <span>¥{{ changeFeeCost }}</span>
        </div>
        <div class="fee-item total">
          <span>需补差价</span>
          <span :class="{ refund: totalCost < 0 }">
            {{ totalCost >= 0 ? `¥${totalCost}` : `退¥${Math.abs(totalCost)}` }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button @click="searchTrains" :loading="searching">重新搜索</el-button>
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :disabled="!canSubmit"
        :loading="submitting"
      >
        {{ submitting ? '提交中...' : '确认改签' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { Right } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  originalTicket: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

const formRef = ref();
const searching = ref(false);
const submitting = ref(false);
const availableTrains = ref([]);

const form = reactive({
  departureDate: new Date(),
  selectedTrain: null,
  selectedSeat: null,
  reason: '',
  reasonDetail: ''
});

const rules = {
  departureDate: [
    { required: true, message: '请选择出发日期', trigger: 'change' }
  ],
  selectedTrain: [
    { required: true, message: '请选择车次', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请选择改签原因', trigger: 'change' }
  ],
  reasonDetail: [
    { required: true, message: '请详细说明改签原因', trigger: 'blur' }
  ]
};

const changeFeeCost = computed(() => {
  // 改签手续费计算逻辑
  return 20; // 固定20元手续费
});

const totalCost = computed(() => {
  if (!form.selectedSeat) return 0;
  const priceDiff = form.selectedSeat.price - props.originalTicket.price;
  return priceDiff + changeFeeCost.value;
});

const canSubmit = computed(() => {
  return form.departureDate && 
         form.selectedTrain && 
         form.selectedSeat && 
         form.reason &&
         (form.reason !== 'OTHER' || form.reasonDetail);
});

const disabledDate = (time) => {
  // 只能选择今天及以后的日期
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

const getTrainType = (trainNumber) => {
  if (trainNumber.startsWith('G')) return '高速';
  if (trainNumber.startsWith('D')) return '动车';
  if (trainNumber.startsWith('C')) return '城际';
  if (trainNumber.startsWith('K')) return '快速';
  if (trainNumber.startsWith('T')) return '特快';
  return '普通';
};

const searchTrains = async () => {
  searching.value = true;
  try {
    // 模拟搜索车次
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 模拟数据
    availableTrains.value = [
      {
        id: 1,
        trainNumber: 'G123',
        departureStation: props.originalTicket.departureStation,
        arrivalStation: props.originalTicket.arrivalStation,
        departureTime: '08:30',
        arrivalTime: '12:45',
        duration: '4小时15分',
        availableSeats: [
          { type: '二等座', price: 553, count: 10 },
          { type: '一等座', price: 888, count: 5 },
          { type: '商务座', price: 1748, count: 2 }
        ]
      },
      {
        id: 2,
        trainNumber: 'G456',
        departureStation: props.originalTicket.departureStation,
        arrivalStation: props.originalTicket.arrivalStation,
        departureTime: '14:20',
        arrivalTime: '18:35',
        duration: '4小时15分',
        availableSeats: [
          { type: '二等座', price: 553, count: 15 },
          { type: '一等座', price: 888, count: 8 },
          { type: '商务座', price: 1748, count: 0 }
        ]
      }
    ];
  } catch (error) {
    ElMessage.error('搜索车次失败');
  } finally {
    searching.value = false;
  }
};

const selectTrain = (train) => {
  form.selectedTrain = train;
  form.selectedSeat = null; // 重置座位选择
};

const selectSeat = (train, seat) => {
  if (seat.count === 0) return;
  form.selectedTrain = train;
  form.selectedSeat = seat;
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    
    submitting.value = true;
    
    const changeData = {
      originalTicket: props.originalTicket,
      newTrain: form.selectedTrain,
      newSeat: form.selectedSeat,
      departureDate: form.departureDate,
      reason: form.reason,
      reasonDetail: form.reasonDetail,
      totalCost: totalCost.value
    };
    
    emit('action', {
      type: 'submit_change_ticket',
      data: changeData
    });
    
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  emit('action', {
    type: 'cancel_change_ticket'
  });
};

onMounted(() => {
  searchTrains();
});
</script>

<style scoped>
.change-ticket-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.tip {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.original-ticket {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.original-ticket h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.ticket-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.train-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.train-type {
  font-size: 12px;
  color: #909399;
}

.route-info {
  flex: 1;
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
}

.station .name {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.arrow {
  color: #909399;
}

.seat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.seat-type {
  font-size: 14px;
  color: #606266;
}

.seat-number {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.change-selection {
  margin-bottom: 30px;
}

.change-selection h4 {
  margin: 0 0 20px 0;
  color: #303133;
}

.no-trains {
  text-align: center;
  padding: 40px 20px;
}

.train-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.train-item {
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.train-item:hover {
  border-color: #409eff;
}

.train-item.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

.train-basic {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
}

.train-basic .train-number {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
  min-width: 80px;
}

.train-route {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.train-route .time {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.train-route .station {
  font-size: 14px;
  color: #606266;
}

.train-route .arrow {
  color: #909399;
}

.duration {
  font-size: 14px;
  color: #909399;
}

.seat-options {
  display: flex;
  gap: 12px;
}

.seat-option {
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  min-width: 80px;
}

.seat-option:hover:not(.unavailable) {
  border-color: #409eff;
  background: #f0f9ff;
}

.seat-option.selected {
  border-color: #409eff;
  background: #409eff;
  color: white;
}

.seat-option.unavailable {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.seat-type {
  font-size: 12px;
  margin-bottom: 2px;
}

.seat-price {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 2px;
}

.seat-count {
  font-size: 11px;
}

.fee-info {
  margin-bottom: 30px;
  padding: 20px;
  background: #fff7e6;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.fee-info h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.fee-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fee-item.total {
  padding-top: 8px;
  border-top: 1px solid #e4e7ed;
  font-weight: bold;
  font-size: 16px;
}

.refund {
  color: #67c23a;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .ticket-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .train-basic {
    flex-direction: column;
    gap: 8px;
  }
  
  .train-route {
    flex-direction: column;
    gap: 4px;
  }
  
  .seat-options {
    flex-wrap: wrap;
  }
}
</style>