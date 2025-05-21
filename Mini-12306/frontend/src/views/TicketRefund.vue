<template>
  <div class="ticket-management">
    <h2>车票退订</h2>
    
    <div class="user-info-bar">
      <div class="user-id">
        用户ID: <el-input v-model="ticketForm.userId" placeholder="请输入用户ID" style="width: 200px" />
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
      <el-table-column label="出发站 → 到达站">
        <template #default="scope">
          {{ scope.row.scheduleInfo?.departureStation }} → {{ scope.row.scheduleInfo?.arrivalStation }}
        </template>
      </el-table-column>
      <el-table-column prop="passengerName" label="乘客姓名" width="100" />
      <el-table-column prop="seatType" label="座位类型" width="90" />
      <el-table-column prop="ticketStatus" label="状态" width="80" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
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
    
    <el-alert
      v-if="tickets.length === 0 && !loading && searched"
      title="您还没有购买任何车票或所有车票已退款/改签"
      type="info"
      show-icon
      :closable="false"
      style="margin-top: 20px"
    />

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

const ticketForm = reactive({
  userId: 'testUser001',
  ticketId: ''
});

const tickets = ref([]);
const refundResult = ref(null);
const errorMessage = ref('');
const loading = ref(false);
const searched = ref(false);
const refundResultDialogVisible = ref(false);

// 在组件加载时查询用户车票
onMounted(() => {
  fetchUserTickets();
});

// 格式化日期
const formatDate = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleDateString();
};

// 行样式
const getRowClassName = (row) => {
  if (row.row.ticketStatus === '已退票' || row.row.ticketStatus === '已改签') {
    return 'inactive-row';
  }
  return '';
};

// 获取用户车票
const fetchUserTickets = async () => {
  if (!ticketForm.userId) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  
  try {
    loading.value = true;
    errorMessage.value = '';
    tickets.value = await api.getUserTickets(ticketForm.userId);
    searched.value = true;
  } catch (error) {
    errorMessage.value = error.error || '获取车票失败';
    ElMessage.error(errorMessage.value);
  } finally {
    loading.value = false;
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
        userId: ticketForm.userId,
        ticketId: ticket.ticketId
      };
      
      refundResult.value = await api.refundTicket(refundData);
      refundResultDialogVisible.value = true;
    } catch (error) {
      errorMessage.value = error.error || error.message || '退票处理失败';
      ElMessage.error(errorMessage.value);
    }
  }).catch(() => {
    // 用户取消操作
  });
};
</script>

<style scoped>
.ticket-management {
  max-width: 1000px;
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

:deep(.inactive-row) {
  color: #909399;
  background-color: #f5f7fa;
}
</style>
