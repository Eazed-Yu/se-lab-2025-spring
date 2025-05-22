<template>
  <div class="search-container">
    <el-form :model="searchForm" label-width="100px" class="search-form">
      <el-form-item label="出发站">
        <el-select v-model="searchForm.departureStation" placeholder="请选择出发站" clearable>
          <el-option v-for="station in stationList" :key="station" :label="station" :value="station" />
        </el-select>
      </el-form-item>
      <el-form-item label="到达站">
        <el-select v-model="searchForm.arrivalStation" placeholder="请选择到达站" clearable>
          <el-option v-for="station in stationList" :key="station" :label="station" :value="station" />
        </el-select>
      </el-form-item>
      <el-form-item label="出发日期">
        <el-date-picker
          v-model="searchForm.departureDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchSchedules">查询车次</el-button>
      </el-form-item>
    </el-form>

    <el-table v-if="schedules.length > 0" :data="schedules" style="width: 100%" stripe>
      <el-table-column prop="trainNumber" label="车次" />
      <el-table-column prop="departureStation" label="出发站" />
      <el-table-column prop="arrivalStation" label="到达站" />
      <el-table-column label="出发时间">
        <template #default="scope">
          {{ formatDateTime(scope.row.departureDateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="到达时间">
        <template #default="scope">
          {{ formatDateTime(scope.row.arrivalDateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="历时">
        <template #default="scope">
          {{ formatDuration(scope.row.durationMinutes) }}
        </template>
      </el-table-column>
      <el-table-column label="座位">
        <template #default="scope">
          <div v-for="(count, type) in scope.row.seatAvailability" :key="type">
            {{ type }}: {{ count }}张
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
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" type="primary" @click="showPurchaseDialog(scope.row)">
            购票
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-else-if="searched" class="no-data">
      没有找到匹配的车次信息
    </div>

    <!-- 购票对话框 -->
    <el-dialog v-model="purchaseDialogVisible" title="购买车票" width="40%">
      <el-form :model="purchaseForm" label-width="120px">
        <el-form-item label="用户ID">
          <el-input v-model="purchaseForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="乘客姓名">
          <el-input v-model="purchaseForm.passengerName" placeholder="请输入乘客姓名" />
        </el-form-item>
        <el-form-item label="乘客身份证号">
          <el-input v-model="purchaseForm.passengerIdCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="座位类型">
          <el-select v-model="purchaseForm.seatType" placeholder="请选择座位类型">
            <el-option
              v-for="(_, seatType) in selectedSchedule?.seatAvailability || {}"
              :key="seatType"
              :label="seatType"
              :value="seatType"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="purchaseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="purchaseTicket">
            确认购票
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 订单成功对话框 -->
    <el-dialog v-model="orderSuccessDialogVisible" title="购票成功" width="50%">
      <div v-if="currentOrder">
        <h3>订单信息</h3>
        <p>订单编号: {{ currentOrder.orderId }}</p>
        <p>订单状态: {{ currentOrder.orderStatus }}</p>
        <p>总金额: {{ currentOrder.totalAmount }}元</p>
        
        <h3>车票信息</h3>
        <el-table :data="currentOrder.tickets" border stripe>
          <el-table-column prop="ticketId" label="车票编号" />
          <el-table-column prop="passengerName" label="乘客姓名" />
          <el-table-column prop="seatType" label="座位类型" />
          <el-table-column prop="seatNumber" label="座位号" />
          <el-table-column prop="pricePaid" label="价格" />
          <el-table-column prop="ticketStatus" label="车票状态" />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="orderSuccessDialogVisible = false">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '../api';

// 站点列表
const stationList = [
  '北京南', '上海虹桥', '武汉', '广州南', '杭州东', '成都东', '重庆北', '北京西', '西安北'
];

// 查询表单数据
const searchForm = reactive({
  departureStation: '',
  arrivalStation: '',
  departureDate: new Date().toISOString().split('T')[0] // 默认今天
});

const schedules = ref([]);
const searched = ref(false);
const purchaseDialogVisible = ref(false);
const orderSuccessDialogVisible = ref(false);
const currentOrder = ref(null);
const selectedSchedule = ref(null);

// 购票表单
const purchaseForm = reactive({
  userId: 'testUser001',
  scheduleId: '',
  passengerName: '',
  passengerIdCard: '',
  seatType: ''
});

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
};

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};

// 查询车次
const searchSchedules = async () => {
  try {
    // 添加表单验证
    if (searchForm.departureStation && searchForm.departureStation === searchForm.arrivalStation) {
      ElMessage.warning('出发站和到达站不能相同');
      return;
    }
    
    const result = await api.querySchedules(searchForm);
    // 确保schedules始终是数组
    schedules.value = Array.isArray(result) ? result : [];
    searched.value = true;
    
    if (schedules.value.length === 0) {
      ElMessage.info('未找到符合条件的车次');
    }
  } catch (error) {
    ElMessage.error(error.error || '查询失败');
  }
};

// 显示购票对话框
const showPurchaseDialog = (schedule) => {
  selectedSchedule.value = schedule;
  purchaseForm.scheduleId = schedule.scheduleId;
  purchaseDialogVisible.value = true;
};

// 购买车票
const purchaseTicket = async () => {
  try {
    currentOrder.value = await api.buyTicket(purchaseForm);
    purchaseDialogVisible.value = false;
    orderSuccessDialogVisible.value = true;
    // 重新查询最新车票数据
    searchSchedules();
  } catch (error) {
    ElMessage.error(error.error || '购票失败');
  }
};

onMounted(() => {
  // 进行一次默认查询
  searchSchedules();
});
</script>

<style scoped>
.search-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-form .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.el-select {
  width: 200px;
}
</style>
