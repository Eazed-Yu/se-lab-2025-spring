<template>
  <div class="passenger-list">
    <div class="header">
      <h3>乘客信息</h3>
      <el-button type="primary" size="small" @click="handleAddPassenger">
        <el-icon><Plus /></el-icon>
        添加乘客
      </el-button>
    </div>
    
    <div v-if="passengers.length === 0" class="empty-state">
      <el-empty description="暂无乘客信息">
        <el-button type="primary" @click="handleAddPassenger">添加乘客</el-button>
      </el-empty>
    </div>
    
    <div v-else class="passenger-cards">
      <div 
        v-for="passenger in passengers" 
        :key="passenger.id" 
        class="passenger-card"
        :class="{ selected: selectedPassengers.includes(passenger.id) }"
        @click="togglePassenger(passenger.id)"
      >
        <div class="passenger-info">
          <div class="name-type">
            <span class="name">{{ passenger.name }}</span>
            <el-tag :type="getPassengerTypeColor(passenger.passengerType)" size="small">
              {{ getPassengerTypeText(passenger.passengerType) }}
            </el-tag>
          </div>
          <div class="id-info">
            <span class="id-type">{{ getIdTypeText(passenger.idType) }}</span>
            <span class="id-number">{{ maskIdNumber(passenger.idNumber) }}</span>
          </div>
          <div class="phone">{{ passenger.phone }}</div>
        </div>
        
        <div class="actions">
          <el-button 
            type="text" 
            size="small" 
            @click.stop="handleEditPassenger(passenger)"
          >
            编辑
          </el-button>
          <el-button 
            type="text" 
            size="small" 
            class="delete-btn"
            @click.stop="handleDeletePassenger(passenger.id)"
          >
            删除
          </el-button>
        </div>
        
        <div v-if="selectedPassengers.includes(passenger.id)" class="selected-indicator">
          <el-icon><Check /></el-icon>
        </div>
      </div>
    </div>
    
    <div v-if="passengers.length > 0" class="footer">
      <el-button type="primary" @click="handleConfirmSelection" :disabled="selectedPassengers.length === 0">
        确认选择 ({{ selectedPassengers.length }})
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Plus, Check } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({
  passengers: {
    type: Array,
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['action']);

const selectedPassengers = ref([]);

const togglePassenger = (passengerId) => {
  if (props.multiple) {
    const index = selectedPassengers.value.indexOf(passengerId);
    if (index > -1) {
      selectedPassengers.value.splice(index, 1);
    } else {
      selectedPassengers.value.push(passengerId);
    }
  } else {
    selectedPassengers.value = selectedPassengers.value.includes(passengerId) ? [] : [passengerId];
  }
};

const handleAddPassenger = () => {
  emit('action', {
    type: 'add_passenger'
  });
};

const handleEditPassenger = (passenger) => {
  emit('action', {
    type: 'edit_passenger',
    data: passenger
  });
};

const handleDeletePassenger = async (passengerId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个乘客信息吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    emit('action', {
      type: 'delete_passenger',
      data: { id: passengerId }
    });
    
    // 从选中列表中移除
    const index = selectedPassengers.value.indexOf(passengerId);
    if (index > -1) {
      selectedPassengers.value.splice(index, 1);
    }
    
    ElMessage.success('删除成功');
  } catch {
    // 用户取消删除
  }
};

const handleConfirmSelection = () => {
  const selected = props.passengers.filter(p => selectedPassengers.value.includes(p.id));
  emit('action', {
    type: 'confirm_passenger_selection',
    data: { passengers: selected }
  });
};

const getPassengerTypeText = (type) => {
  const typeMap = {
    'ADULT': '成人',
    'CHILD': '儿童',
    'STUDENT': '学生'
  };
  return typeMap[type] || type;
};

const getPassengerTypeColor = (type) => {
  const colorMap = {
    'ADULT': '',
    'CHILD': 'warning',
    'STUDENT': 'success'
  };
  return colorMap[type] || '';
};

const getIdTypeText = (type) => {
  const typeMap = {
    'ID_CARD': '身份证',
    'PASSPORT': '护照',
    'HK_MACAO_PASS': '港澳通行证',
    'TAIWAN_PASS': '台湾通行证'
  };
  return typeMap[type] || type;
};

const maskIdNumber = (idNumber) => {
  if (!idNumber) return '';
  if (idNumber.length <= 8) return idNumber;
  return idNumber.substring(0, 4) + '****' + idNumber.substring(idNumber.length - 4);
};
</script>

<style scoped>
.passenger-list {
  max-width: 600px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h3 {
  margin: 0;
  color: #303133;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.passenger-cards {
  display: grid;
  gap: 12px;
  margin-bottom: 20px;
}

.passenger-card {
  position: relative;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.passenger-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.passenger-card.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

.passenger-info {
  margin-bottom: 8px;
}

.name-type {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.id-info {
  display: flex;
  gap: 12px;
  margin-bottom: 4px;
  font-size: 14px;
  color: #606266;
}

.phone {
  font-size: 14px;
  color: #909399;
}

.actions {
  display: flex;
  gap: 8px;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f56c6c;
}

.selected-indicator {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .passenger-card {
    padding: 12px;
  }
  
  .name-type {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .id-info {
    flex-direction: column;
    gap: 4px;
  }
}
</style>