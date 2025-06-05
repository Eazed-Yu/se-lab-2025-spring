<template>
  <div>
    <el-table :data="schedules" style="width: 100%" stripe border>
      <el-table-column prop="trainNumber" label="车次" width="100" align="center" />
      <el-table-column label="出发/到达" width="200">
        <template #default="scope">
          <div class="station-info">
            <div class="departure-station">{{ scope.row.departureStation }}</div>
            <el-divider direction="vertical" />
            <div class="arrival-station">{{ scope.row.arrivalStation }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="280">
        <template #default="scope">
          <div class="time-info">
            <div class="departure-time">{{ formatTime(scope.row.departureDateTime) }}</div>
            <div class="duration">
              <el-divider content-position="center">{{ formatDuration(scope.row.durationMinutes) }}</el-divider>
            </div>
            <div class="arrival-time">{{ formatTime(scope.row.arrivalDateTime) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="座位" min-width="200">
        <template #default="scope">
          <div class="seat-info">
            <el-tag
              v-for="(count, type) in scope.row.seatAvailability"
              :key="type"
              :type="getSeatTagType(count)"
              effect="light"
              class="seat-tag"
            >
              {{ type }}: {{ count > 0 ? `${count}张` : '无票' }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="180">
        <template #default="scope">
          <div class="price-info">
            <div v-for="(price, type) in scope.row.basePrice" :key="type" class="price-item">
              {{ type }}: <span class="price">¥{{ price }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <!--suppress JSValidateTypes -->
          <el-button
            size="small"
            type="primary"
            :disabled="scope.row.status !== '正常'"
            @click="showPurchaseDialog(scope.row)"
            :icon="Ticket"
            circle
            title="购票"
          />
        </template>
      </el-table-column>
    </el-table>
    <!-- 购票对话框 -->
    <div v-if="purchaseDialogVisible">
      <div v-if="selectedSchedule" class="purchase-dialog-content">
        <el-descriptions title="车次信息" :column="3" border>
          <el-descriptions-item label="车次">{{ selectedSchedule.trainNumber }}</el-descriptions-item>
          <el-descriptions-item label="出发站">{{ selectedSchedule.departureStation }}</el-descriptions-item>
          <el-descriptions-item label="到达站">{{ selectedSchedule.arrivalStation }}</el-descriptions-item>
          <el-descriptions-item label="出发时间">{{ formatDateTime(selectedSchedule.departureDateTime) }}</el-descriptions-item>
          <el-descriptions-item label="到达时间">{{ formatDateTime(selectedSchedule.arrivalDateTime) }}</el-descriptions-item>
          <el-descriptions-item label="历时">{{ formatDuration(selectedSchedule.durationMinutes) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="center">乘客信息</el-divider>

        <el-form :model="purchaseForm" label-width="120px" :rules="purchaseRules" ref="purchaseFormRef">

          <el-form-item prop="userId" style="display: none;">
            <el-input v-model="purchaseForm.userId" hidden="hidden" />
          </el-form-item>
          <el-form-item label="选择乘车人" prop="passengerId">
            <el-select
              v-model="purchaseForm.passengerId"
              placeholder="请选择乘车人"
              style="width: 100%"
            >
              <el-option
                v-for="passenger in passengerList"
                :key="passenger.id"
                :label="`${passenger.name} (${passenger.idCard})`"
                :value="passenger.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="座位类型" prop="seatType">
            <el-select v-model="purchaseForm.seatType" placeholder="请选择座位类型" style="width: 100%">
              <el-option
                v-for="(count, seatType) in selectedSchedule.seatAvailability"
                :key="seatType"
                :label="`${seatType} (${count}张) - ¥${selectedSchedule.basePrice[seatType]}`"
                :value="seatType"
                :disabled="count <= 0"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <div v-if="purchaseForm.seatType" class="price-summary">
          <el-alert
            :title="`票价: ¥${getSelectedSeatPrice()}`"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>

      <div>
        <span class="dialog-footer">
          <el-button @click="purchaseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="purchaseTicket" :loading="purchaseLoading">
            确认购票
          </el-button>
        </span>
      </div>
      <!-- 订单成功对话框 -->
      <el-dialog v-model="orderSuccessDialogVisible" title="购票成功" width="60%" destroy-on-close>
        <div v-if="currentOrder" class="order-success-content">
          <el-result icon="success" title="购票成功" sub-title="您的订单已成功提交">
            <template #extra>
              <el-descriptions title="订单信息" :column="2" border>
                <el-descriptions-item label="订单编号">{{ currentOrder.orderId }}</el-descriptions-item>
                <el-descriptions-item label="订单状态">{{ currentOrder.orderStatus }}</el-descriptions-item>
                <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
              </el-descriptions>

              <el-divider content-position="center">车票信息</el-divider>

              <el-table :data="currentOrder.tickets" border stripe>
                <el-table-column prop="ticketId" label="车票编号" width="180" />
                <el-table-column prop="passengerName" label="乘客姓名" width="120" />
                <el-table-column prop="seatType" label="座位类型" width="120" />
                <el-table-column prop="seatNumber" label="座位号" width="120" />
                <el-table-column prop="pricePaid" label="价格" width="100">
                  <template #default="scope">
                    ¥{{ scope.row.pricePaid }}
                  </template>
                </el-table-column>
                <el-table-column prop="ticketStatus" label="车票状态" width="120">
                  <template #default="scope">
                    <el-tag type="success">{{ scope.row.ticketStatus }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </el-result>
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
  </div>
</template>

<script setup>

import { reactive, ref } from 'vue'
import { Ticket } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { passengerApi, ticketApi } from '@/api/index.js'

const { } = defineProps({
  schedules : Array,
})
const purchaseLoading = ref(false);
const schedules = ref([]);
const purchaseDialogVisible = ref(false);
const orderSuccessDialogVisible = ref(false);
const selectedSchedule = ref(null);
const currentOrder = ref(null);
const purchaseFormRef = ref(null);
const passengerList = ref([]);
// 购票表单
const purchaseForm = reactive({
  userId: localStorage.getItem('userId') || '',
  scheduleId: '',
  passengerId: '',
  seatType: ''
});
// 获取选中座位的价格
const getSelectedSeatPrice = () => {
  if (!selectedSchedule.value || !purchaseForm.seatType) return 0;
  return selectedSchedule.value.basePrice[purchaseForm.seatType] || 0;
};

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
};
// 购票表单验证规则
const purchaseRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  passengerId: [
    { required: true, message: '请选择乘车人', trigger: 'change' }
  ],
  seatType: [
    { required: true, message: '请选择座位类型', trigger: 'change' }
  ]
};
// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};
// 格式化时间
const formatTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// 获取座位标签类型
const getSeatTagType = (count) => {
  if (!count || count <= 0) return 'info';
  if (count < 10) return 'warning';
  return 'success';
};

// 显示购票对话框
const showPurchaseDialog = async (schedule) => {
  selectedSchedule.value = schedule;
  purchaseForm.scheduleId = schedule.scheduleId;
  purchaseForm.seatType = ''; // 重置座位类型选择
  purchaseForm.passengerId = ''; // 重置乘车人选择

  // 尝试从本地存储获取用户信息
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo);
      purchaseForm.userId = user.id;
    } catch (e) {
      console.error('Failed to parse user info', e);
    }
  }

  // 加载乘车人列表
  await loadPassengerList();

  purchaseDialogVisible.value = true;
};
// 加载乘车人列表
const loadPassengerList = async () => {
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      ElMessage.error('请先登录');
      return;
    }

    const result = await passengerApi.getPassengerList(userId);
    if (result.success) {
      passengerList.value = result.data || [];

      // 如果有默认乘车人，自动选择
      const defaultPassenger = passengerList.value.find(p => p.isDefault);
      if (defaultPassenger && !purchaseForm.passengerId) {
        purchaseForm.passengerId = defaultPassenger.id;
      }
    } else {
      ElMessage.error(result.message || '获取乘车人列表失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '获取乘车人列表失败');
  }
};

// 购买车票
const purchaseTicket = async () => {
  if (!purchaseFormRef.value) return;

  await purchaseFormRef.value.validate(async (valid) => {
    if (valid) {
      purchaseLoading.value = true;
      try {
        const result = await ticketApi.buyTicket(purchaseForm);

        if (result.data) {
          currentOrder.value = result.data;
          purchaseDialogVisible.value = false;
          orderSuccessDialogVisible.value = true;

          localStorage.setItem('userId', purchaseForm.userId);

          ElMessage.success('购票成功');
        } else {
          ElMessage.warning('购票成功但未返回订单信息');
        }
      } catch (error) {
        console.error('购票错误:', error);
        ElMessage.error(error.message || '购票失败，请稍后重试');
      } finally {
        purchaseLoading.value = false;
      }
    }
  });
};

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo);
      return user.id;
    } catch (e) {
      console.error('Failed to parse user info', e);
    }
  }
  return null;
};
</script>

<style scoped>

</style>