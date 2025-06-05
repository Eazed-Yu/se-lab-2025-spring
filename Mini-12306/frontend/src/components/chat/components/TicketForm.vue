<template>
  <div class="ticket-form">
    <div class="form-header">
      <h4>{{ formTitle }}</h4>
      <div v-if="trainInfo" class="train-summary">
        <span class="train-number">{{ trainInfo.trainNumber }}</span>
        <span class="route">{{ trainInfo.departureStation }} → {{ trainInfo.arrivalStation }}</span>
        <span class="time">{{ trainInfo.departureTime }} - {{ trainInfo.arrivalTime }}</span>
      </div>
    </div>

    <el-form 
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="purchase-form"
    >
      <!-- 座位类型选择 -->
      <el-form-item label="座位类型" prop="seatType">
        <el-select 
          v-model="formData.seatType" 
          placeholder="请选择座位类型"
          style="width: 100%"
          @change="onSeatTypeChange"
        >
          <el-option
            v-for="seat in availableSeatTypes"
            :key="seat.type"
            :label="`${seat.typeName} ¥${seat.price}`"
            :value="seat.type"
            :disabled="seat.availableCount === 0"
          >
            <div class="seat-option">
              <span>{{ seat.typeName }}</span>
              <span class="price">¥{{ seat.price }}</span>
              <span class="count">(余{{ seat.availableCount }}张)</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 乘车人选择 -->
      <el-form-item label="乘车人" prop="passengerId">
        <div class="passenger-section">
          <el-select 
            v-model="formData.passengerId" 
            placeholder="请选择乘车人"
            style="width: 100%"
            @change="onPassengerChange"
          >
            <el-option
              v-for="passenger in passengers"
              :key="passenger.id"
              :label="`${passenger.name} (${passenger.idNumber})`"
              :value="passenger.id"
            >
              <div class="passenger-option">
                <span>{{ passenger.name }}</span>
                <span class="id-number">{{ passenger.idNumber }}</span>
                <el-tag v-if="passenger.isDefault" size="small" type="primary">默认</el-tag>
              </div>
            </el-option>
          </el-select>
          <el-button 
            type="text" 
            @click="addPassenger"
            class="add-passenger-btn"
          >
            + 添加乘车人
          </el-button>
        </div>
      </el-form-item>

      <!-- 联系方式 -->
      <el-form-item label="联系手机" prop="contactPhone">
        <el-input 
          v-model="formData.contactPhone" 
          placeholder="请输入联系手机号"
          maxlength="11"
        />
      </el-form-item>

      <!-- 特殊需求 -->
      <el-form-item label="特殊需求">
        <el-checkbox-group v-model="formData.specialRequests">
          <el-checkbox label="靠窗">靠窗座位</el-checkbox>
          <el-checkbox label="靠过道">靠过道座位</el-checkbox>
          <el-checkbox label="下铺">下铺优先</el-checkbox>
          <el-checkbox label="轮椅">轮椅通道</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 价格信息 -->
      <div v-if="selectedSeatInfo" class="price-info">
        <div class="price-row">
          <span>票价：</span>
          <span class="price">¥{{ selectedSeatInfo.price }}</span>
        </div>
        <div class="price-row">
          <span>服务费：</span>
          <span class="price">¥5</span>
        </div>
        <div class="price-row total">
          <span>总计：</span>
          <span class="price">¥{{ totalPrice }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="cancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitForm"
          :loading="submitting"
          :disabled="!canSubmit"
        >
          {{ data.action === 'change' ? '确认改签' : '立即购票' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElButton, ElCheckboxGroup, ElCheckbox, ElTag, ElMessage } from 'element-plus';

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['action']);

const formRef = ref();
const submitting = ref(false);
const passengers = ref([]);

// 表单数据
const formData = reactive({
  trainScheduleId: props.data.trainScheduleId || '',
  seatType: props.data.seatType || '',
  passengerId: '',
  contactPhone: '',
  specialRequests: []
});

// 表单验证规则
const formRules = {
  seatType: [
    { required: true, message: '请选择座位类型', trigger: 'change' }
  ],
  passengerId: [
    { required: true, message: '请选择乘车人', trigger: 'change' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
};

// 计算属性
const formTitle = computed(() => {
  return props.data.action === 'change' ? '改签车票' : '购买车票';
});

const trainInfo = computed(() => {
  return props.data.trainInfo || null;
});

const availableSeatTypes = computed(() => {
  return trainInfo.value?.seatTypes || [];
});

const selectedSeatInfo = computed(() => {
  if (!formData.seatType) return null;
  return availableSeatTypes.value.find(seat => seat.type === formData.seatType);
});

const totalPrice = computed(() => {
  if (!selectedSeatInfo.value) return 0;
  return selectedSeatInfo.value.price + 5; // 票价 + 服务费
});

const canSubmit = computed(() => {
  return formData.seatType && formData.passengerId && formData.contactPhone && !submitting.value;
});

// 方法
const onSeatTypeChange = (seatType) => {
  console.log('选择座位类型:', seatType);
};

const onPassengerChange = (passengerId) => {
  const passenger = passengers.value.find(p => p.id === passengerId);
  if (passenger && passenger.phone) {
    formData.contactPhone = passenger.phone;
  }
};

const addPassenger = () => {
  emit('action', {
    type: 'add_passenger',
    data: {}
  });
};

const submitForm = async () => {
  try {
    await formRef.value.validate();
    
    submitting.value = true;
    
    const submitData = {
      ...formData,
      totalPrice: totalPrice.value,
      seatInfo: selectedSeatInfo.value
    };
    
    emit('action', {
      type: props.data.action === 'change' ? 'confirm_change' : 'confirm_purchase',
      data: submitData
    });
    
  } catch (error) {
    console.error('表单验证失败:', error);
    ElMessage.error('请检查表单信息');
  } finally {
    submitting.value = false;
  }
};

const cancel = () => {
  emit('action', {
    type: 'cancel_form',
    data: {}
  });
};

// 加载乘车人列表
const loadPassengers = async () => {
  try {
    // 这里应该调用API获取乘车人列表
    // 暂时使用模拟数据
    passengers.value = [
      {
        id: '1',
        name: '张三',
        idNumber: '110101199001011234',
        phone: '13800138000',
        isDefault: true
      },
      {
        id: '2',
        name: '李四',
        idNumber: '110101199002022345',
        phone: '13800138001',
        isDefault: false
      }
    ];
    
    // 自动选择默认乘车人
    const defaultPassenger = passengers.value.find(p => p.isDefault);
    if (defaultPassenger) {
      formData.passengerId = defaultPassenger.id;
      formData.contactPhone = defaultPassenger.phone;
    }
  } catch (error) {
    console.error('加载乘车人列表失败:', error);
    ElMessage.error('加载乘车人信息失败');
  }
};

onMounted(() => {
  loadPassengers();
});
</script>

<style scoped>
.ticket-form {
  width: 100%;
  max-width: 600px;
}

.form-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.form-header h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.train-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
}

.train-number {
  font-weight: bold;
  color: #409eff;
}

.route {
  color: #303133;
}

.time {
  color: #909399;
}

.purchase-form {
  margin-bottom: 20px;
}

.seat-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.price {
  color: #e6a23c;
  font-weight: bold;
}

.count {
  color: #909399;
  font-size: 12px;
}

.passenger-section {
  width: 100%;
}

.passenger-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.id-number {
  color: #909399;
  font-size: 12px;
}

.add-passenger-btn {
  margin-top: 8px;
  color: #409eff;
}

.price-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.price-row.total {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  border-top: 1px solid #e4e7ed;
  padding-top: 8px;
  margin-top: 8px;
}

.price-row .price {
  color: #e6a23c;
  font-weight: bold;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .train-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style>