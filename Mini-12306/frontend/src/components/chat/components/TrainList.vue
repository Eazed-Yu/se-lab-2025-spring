<template>
  <div class="train-list">
    <div class="list-header">
      <h4>车次查询结果</h4>
      <div class="search-info">
        <span>{{ data.departureStation }} → {{ data.arrivalStation }}</span>
        <span class="date">{{ data.departureDate }}</span>
      </div>
    </div>
    
    <div class="train-items">
      <div 
        v-for="train in data.schedules" 
        :key="train.id"
        class="train-item"
        @click="selectTrain(train)"
      >
        <div class="train-info">
          <div class="train-number">{{ train.trainNumber }}</div>
          <div class="train-type">{{ getTrainType(train.trainNumber) }}</div>
        </div>
        
        <div class="time-info">
          <div class="departure">
            <div class="time">{{ train.departureTime }}</div>
            <div class="station">{{ train.departureStation }}</div>
          </div>
          <div class="duration">
            <el-icon><Clock /></el-icon>
            <span>{{ train.duration }}</span>
          </div>
          <div class="arrival">
            <div class="time">{{ train.arrivalTime }}</div>
            <div class="station">{{ train.arrivalStation }}</div>
          </div>
        </div>
        
        <div class="seat-info">
          <div 
            v-for="seat in train.seatTypes" 
            :key="seat.type"
            class="seat-type"
            :class="{ 'no-ticket': seat.availableCount === 0 }"
          >
            <div class="seat-name">{{ seat.typeName }}</div>
            <div class="seat-price">¥{{ seat.price }}</div>
            <div class="seat-count">
              {{ seat.availableCount > 0 ? seat.availableCount : '无票' }}
            </div>
          </div>
        </div>
        
        <div class="actions">
          <el-button 
            type="primary" 
            size="small"
            @click.stop="purchaseTicket(train)"
          >
            购票
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-if="data.schedules.length === 0" class="empty-state">
      <el-empty description="暂无车次信息" />
    </div>
  </div>
</template>

<script setup>
import { ElButton, ElIcon, ElEmpty } from 'element-plus';
import { Clock } from '@element-plus/icons-vue';

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

// 获取车次类型
const getTrainType = (trainNumber) => {
  if (trainNumber.startsWith('G')) return '高速';
  if (trainNumber.startsWith('D')) return '动车';
  if (trainNumber.startsWith('C')) return '城际';
  if (trainNumber.startsWith('Z')) return '直达';
  if (trainNumber.startsWith('T')) return '特快';
  if (trainNumber.startsWith('K')) return '快速';
  return '普通';
};

// 选择车次
const selectTrain = (train) => {
  emit('action', {
    type: 'train_selected',
    data: train
  });
};

// 购票
const purchaseTicket = (train) => {
  emit('action', {
    type: 'purchase_ticket',
    data: {
      trainScheduleId: train.id,
      trainNumber: train.trainNumber,
      departureStation: train.departureStation,
      arrivalStation: train.arrivalStation,
      departureTime: train.departureTime,
      arrivalTime: train.arrivalTime,
      seatTypes: train.seatTypes
    }
  });
};
</script>

<style scoped>
.train-list {
  width: 100%;
  max-width: 800px;
}

.list-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.list-header h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.search-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.date {
  color: #409eff;
  font-weight: 500;
}

.train-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.train-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.train-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.train-info {
  flex: 0 0 100px;
  text-align: center;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.train-type {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.time-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.departure,
.arrival {
  text-align: center;
}

.time {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.station {
  font-size: 14px;
  color: #606266;
}

.duration {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.seat-info {
  flex: 0 0 200px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.seat-type {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  background: #f5f7fa;
}

.seat-type.no-ticket {
  background: #fef0f0;
  color: #f56c6c;
}

.seat-name {
  font-size: 14px;
  color: #606266;
}

.seat-price {
  font-size: 14px;
  font-weight: bold;
  color: #e6a23c;
}

.seat-count {
  font-size: 12px;
  color: #909399;
}

.no-ticket .seat-count {
  color: #f56c6c;
}

.actions {
  flex: 0 0 80px;
  text-align: center;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .train-item {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .train-info,
  .seat-info,
  .actions {
    flex: none;
  }
  
  .time-info {
    padding: 0;
  }
  
  .seat-info {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
}
</style>