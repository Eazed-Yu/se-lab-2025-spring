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
    <el-dialog v-model="changeDialogVisible" title="车票改签" width="80%">
      <div v-if="currentTicket" class="change-ticket-dialog">
        <h3>原车票信息</h3>
        <div class="original-ticket-info">
          <p><strong>车次:</strong> {{ currentTicket.scheduleInfo?.trainNumber }}</p>
          <p><strong>日期:</strong> {{ formatDate(currentTicket.scheduleInfo?.departureDateTime) }}</p>
          <p><strong>区间:</strong> {{ currentTicket.scheduleInfo?.departureStation }} - {{ currentTicket.scheduleInfo?.arrivalStation }}</p>
          <p><strong>时间:</strong> {{ formatTime(currentTicket.scheduleInfo?.departureDateTime) }} - {{ formatTime(currentTicket.scheduleInfo?.arrivalDateTime) }}</p>
          <p><strong>座位类型:</strong> {{ currentTicket.seatType }}</p>
          <p><strong>车票号:</strong> {{ currentTicket.ticketId }}</p>
        </div>
        
        <h3>选择新车次</h3>
        <div class="search-form">
          <el-form :model="changeForm" label-width="100px">
            <el-form-item label="出发日期">
              <el-date-picker
                v-model="changeForm.departureDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-button type="primary" @click="searchNewSchedules">查询可改签车次</el-button>
          </el-form>
        </div>
        
        <el-table 
          v-if="alternativeSchedules.length > 0" 
          :data="alternativeSchedules" 
          style="width: 100%; margin-top: 20px;"
          stripe
          @row-click="selectNewSchedule"
          :row-class-name="row => row.scheduleId === selectedSchedule?.scheduleId ? 'selected-row' : ''"
        >
          <el-table-column prop="trainNumber" label="车次" width="90" />
          <el-table-column prop="departureStation" label="出发站" width="100" />
          <el-table-column prop="arrivalStation" label="到达站" width="100" />
          <el-table-column label="出发时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.departureDateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="到达时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.arrivalDateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="历时">
            <template #default="scope">
              {{ formatDuration(scope.row.durationMinutes) }}
            </template>
          </el-table-column>
          <el-table-column label="可选座位" width="180">
            <template #default="scope">
              <el-select 
                v-if="scope.row.scheduleId === selectedSchedule?.scheduleId"
                v-model="changeForm.seatType" 
                placeholder="选择座位类型"
                style="width: 100%;"
              >
                <el-option
                  v-for="(count, type) in scope.row.seatAvailability"
                  :key="type"
                  :label="`${type} (${count}张)`"
                  :value="type"
                  :disabled="count <= 0"
                />
              </el-select>
              <div v-else>
                <div v-for="(count, type) in scope.row.seatAvailability" :key="type">
                  {{ type }}: {{ count }}张
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格">
            <template #default="scope">
              <div v-for="(price, type) in scope.row.basePrice" :key="type">
                {{ type }}: {{ price }}元
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="alternativeSchedules.length === 0 && searchedAlternatives" class="no-schedules">
          未找到符合条件的可改签车次
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changeDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            :disabled="!selectedSchedule || !changeForm.seatType" 
            @click="confirmChange"
            :loading="changeSubmitting"
          >
            确认改签
          </el-button>
        </span>
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
const searchedAlternatives = ref(false);
const changeSubmitting = ref(false);
const changeResultDialogVisible = ref(false);
const changeResult = ref(null);

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
  alternativeSchedules.value = [];
  changeDialogVisible.value = true;
  searchedAlternatives.value = false;
};

// 查询可改签车次
const searchNewSchedules = async () => {
  if (!currentTicket.value || !changeForm.departureDate) {
    ElMessage.warning('请选择出发日期');
    return;
  }
  
  try {
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
  }
};

// 选择新车次
const selectNewSchedule = (row) => {
  selectedSchedule.value = row;
  changeForm.newScheduleId = row.scheduleId;
  changeForm.seatType = '';
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
</style>
