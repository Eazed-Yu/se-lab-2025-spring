<template>
  <div class="tickets-management">
    <h2>我的车票</h2>
    
    <div class="user-info-bar">
      <el-alert
        v-if="tickets.length === 0 && !loading"
        title="您还没有购买任何车票"
        type="info"
        show-icon
        :closable="false"
      />
      <div class="user-id">
        用户ID: <el-input v-model="userId" placeholder="请输入用户ID" style="width: 200px" />
        <el-button type="primary" @click="fetchUserTickets" :loading="loading">
          查询我的车票
        </el-button>
      </div>
    </div>
    
    <el-table 
      v-if="tickets.length > 0" 
      :data="tickets" 
      style="width: 100%" 
      stripe
      :row-class-name="getRowClassName"
    >
      <el-table-column prop="ticketId" label="车票编号" width="100" />
      <el-table-column label="车次" width="80">
        <template #default="scope">
          {{ scope.row.scheduleInfo?.trainNumber }}
        </template>
      </el-table-column>
      <el-table-column label="日期" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.scheduleInfo?.departureDateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="出发站">
        <template #default="scope">
          {{ scope.row.scheduleInfo?.departureStation }}
        </template>
      </el-table-column>
      <el-table-column label="出发时间" width="100">
        <template #default="scope">
          {{ formatTime(scope.row.scheduleInfo?.departureDateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="到达站">
        <template #default="scope">
          {{ scope.row.scheduleInfo?.arrivalStation }}
        </template>
      </el-table-column>
      <el-table-column label="到达时间" width="100">
        <template #default="scope">
          {{ formatTime(scope.row.scheduleInfo?.arrivalDateTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="passengerName" label="乘客姓名" width="100" />
      <el-table-column prop="seatType" label="座位类型" width="90" />
      <el-table-column prop="seatNumber" label="座位号" width="100" />
      <el-table-column prop="pricePaid" label="票价(元)" width="90" />
      <el-table-column prop="ticketStatus" label="状态" width="80" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            :disabled="scope.row.ticketStatus !== '已出票' && scope.row.ticketStatus !== '已支付'"
            @click="showChangeDialog(scope.row)"
          >
            改签
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            :disabled="scope.row.ticketStatus !== '已出票' && scope.row.ticketStatus !== '已支付'"
            @click="handleRefund(scope.row)"
          >
            退票
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 改签对话框 -->
    <el-dialog v-model="changeDialogVisible" title="车票改签" width="80%" class="modern-dialog">
      <div v-if="currentTicket" class="change-ticket-dialog">
        <el-steps :active="activeStep" finish-status="success" simple class="change-steps">
          <el-step title="选择出发日期" />
          <el-step title="选择新车次和座位" />
          <el-step title="确认改签信息" />
        </el-steps>
        
        <!-- 第一步：原车票信息和选择出发日期 -->
        <div v-if="activeStep === 0" class="step-container">
          <el-card class="original-ticket-card">
            <template #header>
              <div class="card-header">
                <span>原车票信息</span>
                <el-tag size="small" type="info">{{ currentTicket.ticketId }}</el-tag>
              </div>
            </template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="车次">{{ currentTicket.scheduleInfo?.trainNumber }}</el-descriptions-item>
              <el-descriptions-item label="乘客">{{ currentTicket.passengerName }}</el-descriptions-item>
              <el-descriptions-item label="座位">{{ currentTicket.seatType }} / {{ currentTicket.seatNumber }}</el-descriptions-item>
              <el-descriptions-item label="出发站">{{ currentTicket.scheduleInfo?.departureStation }}</el-descriptions-item>
              <el-descriptions-item label="到达站">{{ currentTicket.scheduleInfo?.arrivalStation }}</el-descriptions-item>
              <el-descriptions-item label="票价">{{ currentTicket.pricePaid }}元</el-descriptions-item>
              <el-descriptions-item label="出发日期/时间" :span="3">
                {{ formatDateTime(currentTicket.scheduleInfo?.departureDateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          
          <el-card class="date-selection-card">
            <template #header>
              <div class="card-header">
                <span>选择新的出发日期</span>
              </div>
            </template>
            <div class="date-selection">
              <el-date-picker
                v-model="changeForm.departureDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="disablePastDates"
              />
              <div class="date-selection-hint">
                <el-alert type="info" :closable="false">
                  <template #default>
                    请选择您希望改签的新出发日期，系统将为您查询相同出发站和到达站的其他车次。
                  </template>
                </el-alert>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 第二步：选择新车次和座位 -->
        <div v-if="activeStep === 1" class="step-container">
          <div class="step-header">
            <h3>
              可选车次 
              <el-tag type="info" size="small">{{ currentTicket.scheduleInfo?.departureStation }} → {{ currentTicket.scheduleInfo?.arrivalStation }}</el-tag>
              <el-tag type="info" size="small">{{ changeForm.departureDate }}</el-tag>
            </h3>
          </div>
          
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-else-if="alternativeSchedules.length === 0 && searchedAlternatives" class="no-schedules">
            <el-empty description="未找到符合条件的可改签车次">
              <template #extra>
                <el-button type="primary" @click="activeStep = 0">返回选择其他日期</el-button>
              </template>
            </el-empty>
          </div>
          
          <div v-else class="schedules-container">
            <el-radio-group v-model="selectedScheduleId" class="schedule-radio-group">
              <el-card 
                v-for="schedule in alternativeSchedules" 
                :key="schedule.scheduleId"
                class="schedule-card"
                :class="{ 'selected-schedule': schedule.scheduleId === selectedScheduleId }"
                @click="selectSchedule(schedule)"
              >
                <div class="schedule-card-header">
                  <div class="train-number">{{ schedule.trainNumber }}</div>
                  <div class="schedule-status">
                    <el-tag :type="schedule.status === '正常' ? 'success' : 'warning'" size="small">{{ schedule.status }}</el-tag>
                  </div>
                </div>
                
                <div class="schedule-time-info">
                  <div class="departure">
                    <div class="time">{{ formatTime(schedule.departureDateTime) }}</div>
                    <div class="station">{{ schedule.departureStation }}</div>
                  </div>
                  <div class="duration">
                    <div class="duration-line"></div>
                    <div class="duration-text">{{ formatDuration(schedule.durationMinutes) }}</div>
                  </div>
                  <div class="arrival">
                    <div class="time">{{ formatTime(schedule.arrivalDateTime) }}</div>
                    <div class="station">{{ schedule.arrivalStation }}</div>
                  </div>
                </div>
                
                <div class="schedule-price-seats">
                  <div class="seat-types">
                    <el-select 
                      v-if="schedule.scheduleId === selectedScheduleId"
                      v-model="changeForm.seatType" 
                      placeholder="选择座位类型"
                      style="width: 100%;"
                    >
                      <el-option
                        v-for="(count, type) in schedule.seatAvailability"
                        :key="type"
                        :label="`${type} (${count}张) - ${schedule.basePrice[type]}元`"
                        :value="type"
                        :disabled="count <= 0"
                      >
                        <div class="seat-option">
                          <span>{{ type }}</span>
                          <span class="seat-count">{{ count }}张</span>
                          <span class="seat-price">{{ schedule.basePrice[type] }}元</span>
                        </div>
                      </el-option>
                    </el-select>
                    <div v-else class="seat-summary">
                      <div v-for="(count, type) in schedule.seatAvailability" :key="type" class="seat-type-item">
                        <el-tag :type="count > 0 ? 'success' : 'info'" size="small">
                          {{ type }}: {{ count > 0 ? `${count}张` : '无票' }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-radio-group>
          </div>
        </div>
        
        <!-- 第三步：确认改签信息 -->
        <div v-if="activeStep === 2" class="step-container">
          <el-result icon="info" title="请确认改签信息">
            <template #extra>
              <div class="ticket-change-confirm">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="never" class="comparison-card">
                      <template #header>
                        <div class="card-header">
                          <span>原车票</span>
                          <el-tag size="small" type="info">{{ currentTicket.ticketId }}</el-tag>
                        </div>
                      </template>
                      <el-descriptions :column="1" border>
                        <el-descriptions-item label="车次">{{ currentTicket.scheduleInfo?.trainNumber }}</el-descriptions-item>
                        <el-descriptions-item label="出发/到达">
                          {{ currentTicket.scheduleInfo?.departureStation }} → {{ currentTicket.scheduleInfo?.arrivalStation }}
                        </el-descriptions-item>
                        <el-descriptions-item label="出发时间">
                          {{ formatDateTime(currentTicket.scheduleInfo?.departureDateTime) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="座位类型">{{ currentTicket.seatType }}</el-descriptions-item>
                        <el-descriptions-item label="票价">{{ currentTicket.pricePaid }}元</el-descriptions-item>
                      </el-descriptions>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card shadow="never" class="comparison-card">
                      <template #header>
                        <div class="card-header">
                          <span>新车票</span>
                          <el-tag size="small" type="success">待出票</el-tag>
                        </div>
                      </template>
                      <el-descriptions :column="1" border>
                        <el-descriptions-item label="车次">{{ selectedSchedule?.trainNumber }}</el-descriptions-item>
                        <el-descriptions-item label="出发/到达">
                          {{ selectedSchedule?.departureStation }} → {{ selectedSchedule?.arrivalStation }}
                        </el-descriptions-item>
                        <el-descriptions-item label="出发时间">
                          {{ formatDateTime(selectedSchedule?.departureDateTime) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="座位类型">{{ changeForm.seatType }}</el-descriptions-item>
                        <el-descriptions-item label="预计票价">{{ getPriceForSelectedSeat() }}元</el-descriptions-item>
                      </el-descriptions>
                    </el-card>
                  </el-col>
                </el-row>
                
                <div class="price-difference" v-if="getPriceDifference() !== 0">
                  <el-alert
                    :title="`票价${getPriceDifference() > 0 ? '差额' : '退款'}: ${Math.abs(getPriceDifference())}元`"
                    :type="getPriceDifference() > 0 ? 'warning' : 'success'"
                    :description="getPriceDifference() > 0 ? '改签将需要补差价' : '改签后将退回差价'"
                    show-icon
                  />
                </div>
                
                <div class="change-policy">
                  <el-alert
                    title="改签政策说明"
                    type="info"
                    :closable="false"
                    description="1. 改签只能选择相同起始站和目的站的车次；2. 同一天内首次改签免收手续费，第二次起收取5%手续费；3. 不同日期改签可能会有票价差异；4. 改签成功后原车票将自动作废。"
                    show-icon
                  />
                </div>
              </div>
            </template>
          </el-result>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <div>
            <el-button @click="activeStep > 0 ? activeStep-- : changeDialogVisible = false">
              {{ activeStep > 0 ? '上一步' : '取消' }}
            </el-button>
          </div>
          <div>
            <el-button 
              v-if="activeStep < 2" 
              type="primary" 
              @click="handleNextStep"
              :disabled="(activeStep === 0 && !changeForm.departureDate) || 
                        (activeStep === 1 && (!selectedScheduleId || !changeForm.seatType))"
            >
              下一步
            </el-button>
            <el-button 
              v-else 
              type="primary" 
              @click="confirmChange"
              :loading="changeSubmitting"
            >
              确认改签
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
    
    <!-- 改签结果对话框 -->
    <el-dialog v-model="changeResultDialogVisible" title="改签成功" width="50%">
      <div v-if="changeResult">
        <el-result icon="success" :title="changeResult.message">
          <template #extra>
            <div class="ticket-change-result">
              <p><strong>原车票号:</strong> {{ changeResult.oldTicketId }}</p>
              <p><strong>新车票号:</strong> {{ changeResult.newTicketId }}</p>
              
              <h4>新车票信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="车次">{{ changeResult.newTicket.scheduleInfo.trainNumber }}</el-descriptions-item>
                <el-descriptions-item label="乘客">{{ changeResult.newTicket.passengerName }}</el-descriptions-item>
                <el-descriptions-item label="出发站">{{ changeResult.newTicket.scheduleInfo.departureStation }}</el-descriptions-item>
                <el-descriptions-item label="到达站">{{ changeResult.newTicket.scheduleInfo.arrivalStation }}</el-descriptions-item>
                <el-descriptions-item label="出发时间">{{ formatDateTime(changeResult.newTicket.scheduleInfo.departureDateTime) }}</el-descriptions-item>
                <el-descriptions-item label="到达时间">{{ formatDateTime(changeResult.newTicket.scheduleInfo.arrivalDateTime) }}</el-descriptions-item>
                <el-descriptions-item label="座位类型">{{ changeResult.newTicket.seatType }}</el-descriptions-item>
                <el-descriptions-item label="座位号">{{ changeResult.newTicket.seatNumber }}</el-descriptions-item>
                <el-descriptions-item label="票价">{{ changeResult.newTicket.pricePaid }}元</el-descriptions-item>
                <el-descriptions-item label="状态">{{ changeResult.newTicket.ticketStatus }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-result>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="changeResultDialogVisible = false; fetchUserTickets();">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 退票结果对话框 -->
    <el-dialog v-model="refundResultDialogVisible" title="退票结果" width="40%">
      <el-result
        v-if="refundResult"
        icon="success"
        :title="refundResult.message"
        :sub-title="`车票ID: ${refundResult.ticketId} | 新状态: ${refundResult.newTicketStatus}`"
      >
      </el-result>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="refundResultDialogVisible = false; fetchUserTickets();">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="false"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { api } from '../api';

const userId = ref('testUser001');
const tickets = ref([]);
const loading = ref(false);
const errorMessage = ref('');

// 改签相关
const changeDialogVisible = ref(false);
const currentTicket = ref(null);
const alternativeSchedules = ref([]);
const selectedSchedule = ref(null);
const selectedScheduleId = ref('');
const searchedAlternatives = ref(false);
const changeSubmitting = ref(false);
const changeResultDialogVisible = ref(false);
const changeResult = ref(null);
const activeStep = ref(0);  // 步骤控制

// 退票相关
const refundResultDialogVisible = ref(false);
const refundResult = ref(null);

const changeForm = reactive({
  userId: '',
  ticketId: '',
  departureDate: '',
  newScheduleId: '',
  seatType: ''
});

onMounted(() => {
  fetchUserTickets();
});

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
};

// 格式化日期
const formatDate = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleDateString();
};

// 格式化时间
const formatTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};

// 获取用户车票
const fetchUserTickets = async () => {
  if (!userId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  
  try {
    loading.value = true;
    errorMessage.value = '';
    tickets.value = await api.getUserTickets(userId.value);
  } catch (error) {
    errorMessage.value = error.error || '获取车票失败';
    ElMessage.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
};

// 行样式
const getRowClassName = (row) => {
  if (row.row.ticketStatus === '已退票' || row.row.ticketStatus === '已改签') {
    return 'inactive-row';
  }
  return '';
};

// 显示改签对话框
const showChangeDialog = (ticket) => {
  currentTicket.value = ticket;
  changeForm.userId = userId.value;
  changeForm.ticketId = ticket.ticketId;
  changeForm.departureDate = formatDate(ticket.scheduleInfo?.departureDateTime);
  changeForm.seatType = '';
  selectedSchedule.value = null;
  selectedScheduleId.value = '';
  alternativeSchedules.value = [];
  changeDialogVisible.value = true;
  searchedAlternatives.value = false;
  activeStep.value = 0;  // 重置步骤
};

// 处理步骤切换
const handleNextStep = async () => {
  if (activeStep.value === 0) {
    // 从第一步到第二步：查询可改签车次
    await searchNewSchedules();
    if (alternativeSchedules.value.length > 0) {
      activeStep.value = 1;
    }
  } else if (activeStep.value === 1 && selectedSchedule.value && changeForm.seatType) {
    // 从第二步到第三步：确认信息
    activeStep.value = 2;
  }
};

// 查询可改签车次
const searchNewSchedules = async () => {
  if (!currentTicket.value || !changeForm.departureDate) {
    ElMessage.warning('请选择出发日期');
    return;
  }
  
  try {
    loading.value = true;
    const searchParams = {
      departureStation: currentTicket.value.scheduleInfo.departureStation,
      arrivalStation: currentTicket.value.scheduleInfo.arrivalStation,
      departureDate: changeForm.departureDate
    };
    
    const result = await api.querySchedules(searchParams);
    // 确保alternativeSchedules始终是数组
    alternativeSchedules.value = Array.isArray(result) ? result : [];
    searchedAlternatives.value = true;
    
    // 过滤掉原车次
    alternativeSchedules.value = alternativeSchedules.value.filter(
      schedule => schedule.scheduleId !== currentTicket.value.scheduleInfo.scheduleId
    );
    
    if (alternativeSchedules.value.length === 0) {
      ElMessage.info('未找到符合条件的可改签车次');
    }
  } catch (error) {
    ElMessage.error(error.error || '查询车次失败');
  } finally {
    loading.value = false;
  }
};

// 选择新车次
const selectSchedule = (schedule) => {
  selectedSchedule.value = schedule;
  selectedScheduleId.value = schedule.scheduleId;
  changeForm.newScheduleId = schedule.scheduleId;
  changeForm.seatType = '';
};

// 计算所选座位的价格
const getPriceForSelectedSeat = () => {
  if (!selectedSchedule.value || !changeForm.seatType) return 0;
  return selectedSchedule.value.basePrice[changeForm.seatType] || 0;
};

// 计算价格差异
const getPriceDifference = () => {
  const newPrice = getPriceForSelectedSeat();
  const oldPrice = currentTicket.value?.pricePaid || 0;
  return newPrice - oldPrice;
};

// 禁用过去的日期
const disablePastDates = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0));
};

// 确认改签
const confirmChange = async () => {
  if (!selectedSchedule.value || !changeForm.seatType) {
    ElMessage.warning('请选择车次和座位类型');
    return;
  }
  
  try {
    changeSubmitting.value = true;
    const changeRequest = {
      userId: userId.value,
      ticketId: currentTicket.value.ticketId,
      newScheduleId: selectedSchedule.value.scheduleId,
      seatType: changeForm.seatType
    };
    
    changeResult.value = await api.changeTicket(changeRequest);
    changeDialogVisible.value = false;
    changeResultDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error.error || '改签失败');
  } finally {
    changeSubmitting.value = false;
  }
};

// 退票
const handleRefund = (ticket) => {
  ElMessageBox.confirm(
    `确定要退票吗？车票号: ${ticket.ticketId}，行程: ${ticket.scheduleInfo.departureStation} - ${ticket.scheduleInfo.arrivalStation}`,
    '退票确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const refundData = {
        userId: userId.value,
        ticketId: ticket.ticketId
      };
      
      refundResult.value = await api.refundTicket(refundData);
      refundResultDialogVisible.value = true;
    } catch (error) {
      ElMessage.error(error.error || '退票失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};
</script>

<style scoped>
.tickets-management {
  max-width: 1500px;
  margin: 0 auto;
  padding: 20px;
}

.user-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.user-id {
  display: flex;
  align-items: center;
  gap: 10px;
}

.change-ticket-dialog {
  max-height: 60vh;
  overflow-y: auto;
}

.original-ticket-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.search-form {
  margin: 20px 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.no-schedules {
  text-align: center;
  padding: 30px;
  color: #909399;
}

.ticket-change-result {
  margin-top: 20px;
}

:deep(.inactive-row) {
  color: #909399;
  background-color: #f5f7fa;
}

:deep(.selected-row) {
  background-color: #ecf5ff;
}

/* 新添加的现代化样式 */
.modern-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 30px;
  }
}

.change-steps {
  margin-bottom: 30px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.step-container {
  min-height: 300px;
  margin-bottom: 20px;
}

.original-ticket-card, .date-selection-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-selection {
  padding: 20px 0;
}

.date-selection-hint {
  margin-top: 15px;
}

.loading-container {
  padding: 40px 0;
  text-align: center;
}

.schedules-container {
  margin-top: 20px;
}

.schedule-radio-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.schedule-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 0;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }
  
  &.selected-schedule {
    border-color: #409eff;
    background-color: #ecf5ff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}

.schedule-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.train-number {
  font-size: 16px;
  font-weight: bold;
}

.schedule-time-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.departure, .arrival {
  text-align: center;
  flex: 1;
}

.time {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.station {
  color: #606266;
}

.duration {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 2;
}

.duration-line {
  width: 100%;
  height: 2px;
  background-color: #dcdfe6;
  position: relative;
  
  &:before, &:after {
    content: "";
    position: absolute;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #409eff;
    top: -3px;
  }
  
  &:before {
    left: 0;
  }
  
  &:after {
    right: 0;
  }
}

.duration-text {
  margin-top: 10px;
  color: #606266;
  font-size: 12px;
}

.schedule-price-seats {
  margin-top: 15px;
}

.seat-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.seat-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.seat-count {
  color: #67c23a;
}

.seat-price {
  font-weight: bold;
}

.step-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comparison-card {
  height: 100%;
  
  :deep(.el-descriptions__label) {
    width: 100px;
  }
}

.price-difference {
  margin: 20px 0;
}

.change-policy {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.ticket-change-confirm {
  text-align: left;
  margin: 20px 0;
}
</style>
