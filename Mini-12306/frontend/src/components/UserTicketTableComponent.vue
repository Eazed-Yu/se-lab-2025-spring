<template>
  <div>
    <el-table :data="tickets" style="width: 100%" stripe border>
      <el-table-column prop="scheduleInfo.trainNumber" label="车次" width="80" align="center" />
      <el-table-column label="行程" width="150">
        <template #default="scope">
          <div class="route-info">
            <div class="departure-station">{{ scope.row.scheduleInfo.departureStation }}</div>
            <el-divider direction="vertical" />
            <div class="arrival-station">{{ scope.row.scheduleInfo.arrivalStation }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="200">
        <template #default="scope">
          <div class="time-info">
            <div class="departure-time">{{ formatTime(scope.row.scheduleInfo.departureDateTime) }}</div>
            <div class="duration">
              <el-divider content-position="center">{{ formatDuration(scope.row.scheduleInfo.durationMinutes) }}</el-divider>
            </div>
            <div class="arrival-time">{{ formatTime(scope.row.scheduleInfo.arrivalDateTime) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="乘客信息" width="120">
        <template #default="scope">
          <div class="passenger-info">
            <div>{{ scope.row.passengerName }}</div>
            <div class="id-card">{{ maskIdCard(scope.row.passengerIdCard) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="座位信息" width="150">
        <template #default="scope">
          <div class="seat-info">
            <el-tag effect="plain">{{ scope.row.seatType }}</el-tag>
            <div class="seat-number">{{ scope.row.seatNumber }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="pricePaid" label="票价" width="100">
        <template #default="scope">
          <span class="price">¥{{ scope.row.pricePaid }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ticketStatus" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.ticketStatus)">
            {{ scope.row.ticketStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <div class="action-buttons">
            <el-button 
              size="small" 
              type="primary" 
              :disabled="!canOperateTicket(scope.row)"
              @click="showChangeDialog(scope.row)"
              :icon="RefreshRight"
              circle
              title="改签"
            />
            <el-button 
              size="small" 
              type="danger" 
              :disabled="!canOperateTicket(scope.row)"
              @click="showRefundDialog(scope.row)"
              :icon="Delete"
              circle
              title="退票"
            />
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 改签对话框 -->
    <el-dialog
      v-model="changeDialogVisible"
      :title="`改签 - 步骤 ${changeStep}：${changeStepLabels[changeStep - 1]}`"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-steps :active="changeStep" finish-status="success" simple>
        <el-step v-for="(label, index) in changeStepLabels" :key="index" :title="label" />
      </el-steps>

      <div class="change-dialog-content">
        <!-- 步骤1：选择日期 -->
        <div v-if="changeStep === 1">
          <div class="ticket-info">
            <h4>原车票信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="车次">{{ selectedTicket?.scheduleInfo.trainNumber }}</el-descriptions-item>
              <el-descriptions-item label="行程">{{ selectedTicket?.scheduleInfo.departureStation }} - {{ selectedTicket?.scheduleInfo.arrivalStation }}</el-descriptions-item>
              <el-descriptions-item label="出发时间">{{ formatDateTime(selectedTicket?.scheduleInfo.departureDateTime) }}</el-descriptions-item>
              <el-descriptions-item label="到达时间">{{ formatDateTime(selectedTicket?.scheduleInfo.arrivalDateTime) }}</el-descriptions-item>
              <el-descriptions-item label="座位类型">{{ selectedTicket?.seatType }}</el-descriptions-item>
              <el-descriptions-item label="座位号">{{ selectedTicket?.seatNumber }}</el-descriptions-item>
              <el-descriptions-item label="票价">¥{{ selectedTicket?.pricePaid }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="date-selection">
            <el-date-picker
              v-model="changeForm.newDepartureDate"
              type="date"
              placeholder="选择改签日期"
              :disabled-date="disablePastDates"
              :shortcuts="[
                {
                  text: '今天',
                  value: new Date()
                },
                {
                  text: '明天',
                  value: () => {
                    const date = new Date();
                    date.setDate(date.getDate() + 1);
                    return date;
                  }
                },
                {
                  text: '一周后',
                  value: () => {
                    const date = new Date();
                    date.setDate(date.getDate() + 7);
                    return date;
                  }
                }
              ]"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </div>
        </div>

        <!-- 步骤2：选择车次 -->
        <div v-if="changeStep === 2">
          <el-table 
            :data="alternativeSchedules" 
            style="width: 100%" 
            @row-click="selectSchedule" 
            highlight-current-row
            v-loading="changeLoading"
          >
            <el-table-column prop="trainNumber" label="车次" width="100" align="center" />
            <el-table-column label="行程" width="200">
              <template #default="{ row }">
                <div class="route-info">
                  <div class="departure-station">{{ row.departureStation }}</div>
                  <el-divider direction="vertical" />
                  <div class="arrival-station">{{ row.arrivalStation }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="280">
              <template #default="{ row }">
                <div class="time-info">
                  <div class="departure-time">{{ formatTime(row.departureDateTime) }}</div>
                  <div class="duration">
                    <el-divider content-position="center">{{ formatDuration(row.durationMinutes) }}</el-divider>
                  </div>
                  <div class="arrival-time">{{ formatTime(row.arrivalDateTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="座位" width="200">
              <template #default="{ row }">
                <div class="seat-tags">
                  <el-tag 
                    v-for="(count, type) in row.seatAvailability" 
                    :key="type"
                    :type="getSeatTagType(count)"
                    class="seat-tag"
                  >
                    {{ type }}: {{ count }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="票价" width="200">
              <template #default="{ row }">
                <div class="price-info">
                  <div v-for="(price, type) in row.basePrice" :key="type" class="price-item">
                    {{ type }}: <span class="price">¥{{ price }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="seat-selection" v-if="selectedSchedule">
            <h4>选择座位类型</h4>
            <el-radio-group v-model="changeForm.newSeatType">
              <el-radio 
                v-for="(count, type) in selectedSchedule.seatAvailability" 
                :key="type" 
                :label="type"
                :disabled="count <= 0"
              >
                {{ type }} (¥{{ selectedSchedule.basePrice[type] }})
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 步骤3：确认改签 -->
        <div v-if="changeStep === 3 && selectedSchedule && selectedTicket">
          <el-alert
            title="改签须知"
            type="info"
            description="1. 改签后，原车票作废；2. 如改签后的票价高于原票价，需补差价；3. 如改签后的票价低于原票价，差价将退还。"
            :closable="false"
            show-icon
          />

          <el-row :gutter="20" class="change-info">
            <el-col :span="12">
              <h4>原车票信息</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="车次">{{ selectedTicket.scheduleInfo.trainNumber }}</el-descriptions-item>
                <el-descriptions-item label="行程">{{ selectedTicket.scheduleInfo.departureStation }} - {{ selectedTicket.scheduleInfo.arrivalStation }}</el-descriptions-item>
                <el-descriptions-item label="出发时间">{{ formatDateTime(selectedTicket.scheduleInfo.departureDateTime) }}</el-descriptions-item>
                <el-descriptions-item label="座位类型">{{ selectedTicket.seatType }}</el-descriptions-item>
                <el-descriptions-item label="票价">¥{{ selectedTicket.pricePaid }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="12">
              <h4>新车票信息</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="车次">{{ selectedSchedule.trainNumber }}</el-descriptions-item>
                <el-descriptions-item label="行程">{{ selectedSchedule.departureStation }} - {{ selectedSchedule.arrivalStation }}</el-descriptions-item>
                <el-descriptions-item label="出发时间">{{ formatDateTime(selectedSchedule.departureDateTime) }}</el-descriptions-item>
                <el-descriptions-item label="座位类型">{{ changeForm.newSeatType }}</el-descriptions-item>
                <el-descriptions-item label="票价">¥{{ selectedSchedule.basePrice[changeForm.newSeatType] }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>

          <div class="price-difference">
            <h4>价格差额</h4>
            <el-alert
              :title="getPriceDifference() > 0 ? `需补差价: ¥${getPriceDifference()}` : `退还差价: ¥${Math.abs(getPriceDifference())}`"
              :type="getPriceDifference() > 0 ? 'warning' : 'success'"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changeDialogVisible = false">取消</el-button>
          <el-button v-if="changeStep > 1" @click="goToPreviousStep">上一步</el-button>
          <el-button 
            v-if="changeStep === 1" 
            type="primary" 
            @click="queryAlternativeSchedules" 
            :disabled="!changeForm.newDepartureDate"
            :loading="changeLoading"
          >
            查询可改签车次
          </el-button>
          <el-button 
            v-if="changeStep === 2" 
            type="primary" 
            @click="goToConfirmStep" 
            :disabled="!canProceedToNextStep"
          >
            下一步
          </el-button>
          <el-button 
            v-if="changeStep === 3" 
            type="primary" 
            @click="confirmChange"
            :loading="changeConfirmLoading"
          >
            确认改签
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退票对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="退票确认"
      width="50%"
    >
      <div class="refund-dialog-content">
        <el-alert
          title="退票须知"
          type="info"
          description="1. 开车前24小时以上退票，收取票价5%的退票费；2. 开车前24小时以内退票，收取票价10%的退票费；3. 开车后不予退票。"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <div class="ticket-info">
          <h4>车票信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="车票编号">{{ selectedTicket?.ticketId }}</el-descriptions-item>
            <el-descriptions-item label="车次">{{ selectedTicket?.scheduleInfo.trainNumber }}</el-descriptions-item>
            <el-descriptions-item label="行程">{{ selectedTicket?.scheduleInfo.departureStation }} - {{ selectedTicket?.scheduleInfo.arrivalStation }}</el-descriptions-item>
            <el-descriptions-item label="出发时间">{{ formatDateTime(selectedTicket?.scheduleInfo.departureDateTime) }}</el-descriptions-item>
            <el-descriptions-item label="座位类型">{{ selectedTicket?.seatType }}</el-descriptions-item>
            <el-descriptions-item label="座位号">{{ selectedTicket?.seatNumber }}</el-descriptions-item>
            <el-descriptions-item label="乘客姓名">{{ selectedTicket?.passengerName }}</el-descriptions-item>
            <el-descriptions-item label="乘客身份证">{{ maskIdCard(selectedTicket?.passengerIdCard) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="refund-calculation">
          <h4>退款计算</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="票价">¥{{ selectedTicket?.pricePaid }}</el-descriptions-item>
            <el-descriptions-item label="退票手续费">¥{{ calculateRefundFee() }}</el-descriptions-item>
            <el-descriptions-item label="退款金额">
              <span class="price">¥{{ calculateRefundAmount() }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmRefund" :loading="refundLoading">确认退票</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      :title="successTitle"
      width="30%"
    >
      <div class="success-dialog-content">
        <el-result icon="success" :title="successTitle" :sub-title="successMessage" />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="closeSuccessDialog">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { RefreshRight, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ticketApi } from '@/api/index.js'

const { data } = defineProps({
  data: Object,
})

const emit = defineEmits(['action', 'refresh'])

const tickets = ref(data.tickets || [])
const changeLoading = ref(false)
const changeConfirmLoading = ref(false)
const refundLoading = ref(false)
const changeDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const successDialogVisible = ref(false)
const selectedTicket = ref(null)
const alternativeSchedules = ref([])
const selectedSchedule = ref(null)
const changeStep = ref(1)
const successTitle = ref('')
const successMessage = ref('')

// 改签表单数据
const changeForm = reactive({
  ticketId: '',
  newDepartureDate: '',
  newScheduleId: '',
  newSeatType: ''
})

// 改签步骤标签
const changeStepLabels = ['选择日期', '选择车次', '确认改签']

// 计算属性
const canProceedToNextStep = computed(() => {
  if (changeStep.value === 2) {
    return selectedSchedule.value && changeForm.newSeatType
  }
  return true
})

// 日期处理函数
const disablePastDates = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

// 格式化函数
const formatTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
}

const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return ''
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

const maskIdCard = (idCard) => {
  if (!idCard) return ''
  return idCard.replace(/^(.{4})(?:\d+)(.{4})$/, '$1********$2')
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '已出票':
      return 'success'
    case '已退票':
      return 'danger'
    case '已改签':
      return 'warning'
    case '已过期':
      return 'info'
    default:
      return 'info'
  }
}

// 获取座位标签类型
const getSeatTagType = (count) => {
  if (!count || count <= 0) return 'info'
  if (count < 10) return 'warning'
  return 'success'
}

// 修改按钮的禁用条件判断函数
const canOperateTicket = (ticket) => {
  if (!ticket) return false
  
  // 检查车票状态
  const isStatusValid = ticket.ticketStatus !== '已出票'
  
  // 检查出发时间
  const departureTime = new Date(ticket.scheduleInfo?.departureDateTime)
  const now = new Date()
  const isTimeValid = departureTime > now
  
  return isStatusValid && isTimeValid
}

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('user')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id
    } catch (e) {
      console.error('Failed to parse user info', e)
      return ''
    }
  }
  return ''
}

// 计算价格差额
const getPriceDifference = () => {
  if (!selectedTicket.value || !selectedSchedule.value || !changeForm.newSeatType) return 0
  const oldPrice = selectedTicket.value.pricePaid
  const newPrice = selectedSchedule.value.basePrice[changeForm.newSeatType] || 0
  return newPrice - oldPrice
}

// 计算退票费用
const calculateRefundFee = () => {
  if (!selectedTicket.value) return 0
  const departureTime = new Date(selectedTicket.value.scheduleInfo.departureDateTime)
  const now = new Date()
  const hoursDiff = (departureTime - now) / (1000 * 60 * 60)
  
  if (hoursDiff <= 0) {
    return selectedTicket.value.pricePaid // 开车后不予退票
  } else if (hoursDiff <= 24) {
    return selectedTicket.value.pricePaid * 0.1 // 24小时内收取10%
  } else {
    return selectedTicket.value.pricePaid * 0.05 // 24小时以上收取5%
  }
}

// 计算退款金额
const calculateRefundAmount = () => {
  if (!selectedTicket.value) return 0
  return selectedTicket.value.pricePaid - calculateRefundFee()
}

// 显示改签对话框
const showChangeDialog = (ticket) => {
  selectedTicket.value = ticket
  changeForm.ticketId = ticket.ticketId
  changeForm.newDepartureDate = ''
  changeForm.newScheduleId = ''
  changeForm.newSeatType = ''
  changeStep.value = 1
  selectedSchedule.value = null
  alternativeSchedules.value = []
  changeDialogVisible.value = true
}

// 显示退票对话框
const showRefundDialog = (ticket) => {
  selectedTicket.value = ticket
  refundDialogVisible.value = true
}

// 改签上一步
const goToPreviousStep = () => {
  if (changeStep.value > 1) {
    changeStep.value--
    if (changeStep.value === 1) {
      selectedSchedule.value = null
      changeForm.newSeatType = ''
    }
  }
}

// 改签确认步骤
const goToConfirmStep = () => {
  if (!selectedSchedule.value || !changeForm.newSeatType) {
    ElMessage.warning('请选择座位类型')
    return
  }
  changeStep.value = 3
}

// 查询可替代车次
const queryAlternativeSchedules = async () => {
  if (!selectedTicket.value || !changeForm.newDepartureDate) return
  
  changeLoading.value = true
  try {
    const result = await ticketApi.querySchedules({
      departureStation: selectedTicket.value.scheduleInfo.departureStation,
      arrivalStation: selectedTicket.value.scheduleInfo.arrivalStation,
      departureDate: changeForm.newDepartureDate
    })
    
    if (result.data && Array.isArray(result.data)) {
      alternativeSchedules.value = result.data.map(schedule => ({
        scheduleId: schedule.scheduleId,
        trainNumber: schedule.trainNumber,
        departureStation: schedule.departureStation,
        arrivalStation: schedule.arrivalStation,
        departureDateTime: schedule.departureDateTime,
        arrivalDateTime: schedule.arrivalDateTime,
        durationMinutes: schedule.durationMinutes,
        seatAvailability: schedule.seatAvailability || {},
        basePrice: schedule.basePrice || {},
        status: schedule.status
      }))
      
      if (alternativeSchedules.value.length > 0) {
        changeStep.value = 2
      } else {
        ElMessage.info('未找到可改签的车次')
      }
    } else {
      alternativeSchedules.value = []
      ElMessage.info('未找到可改签的车次')
    }
  } catch (error) {
    console.error('查询可替代车次错误:', error)
    ElMessage.error(error.message || '查询失败，请稍后重试')
    alternativeSchedules.value = []
  } finally {
    changeLoading.value = false
  }
}

// 选择车次
const selectSchedule = (row) => {
  if (!row) return
  selectedSchedule.value = row
  changeForm.newScheduleId = row.scheduleId
  changeForm.newSeatType = ''
}

// 改签车票
const confirmChange = async () => {
  if (!selectedTicket.value || !selectedSchedule.value || !changeForm.newSeatType) {
    ElMessage.warning('请完成所有选择')
    return
  }
  
  changeConfirmLoading.value = true
  try {
    const changeData = {
      userId: getCurrentUserId(),
      ticketId: selectedTicket.value.ticketId,
      newScheduleId: selectedSchedule.value.scheduleId,
      seatType: changeForm.newSeatType
    }
    
    const result = await ticketApi.changeTicket(changeData)
    
    changeDialogVisible.value = false
    successTitle.value = '改签成功'
    successMessage.value = result.message || '您的车票已成功改签'
    successDialogVisible.value = true
    
    ElMessage.success('改签成功')
    emit('refresh')
  } catch (error) {
    console.error('改签错误:', error)
    ElMessage.error(error.message || '改签失败，请稍后重试')
  } finally {
    changeConfirmLoading.value = false
  }
}

// 退票
const confirmRefund = async () => {
  if (!selectedTicket.value) return
  
  refundLoading.value = true
  try {
    const refundData = {
      userId: getCurrentUserId(),
      ticketId: selectedTicket.value.ticketId
    }
    
    const result = await ticketApi.refundTicket(refundData)
    
    refundDialogVisible.value = false
    successTitle.value = '退票成功'
    successMessage.value = result.message || '退款金额将在1-7个工作日内退回您的支付账户'
    successDialogVisible.value = true
    
    ElMessage.success('退票成功')
    emit('refresh')
  } catch (error) {
    ElMessage.error(error.message || '退票失败，请稍后重试')
  } finally {
    refundLoading.value = false
  }
}

// 关闭成功对话框
const closeSuccessDialog = () => {
  successDialogVisible.value = false
  emit('refresh')
}
</script>

<style scoped>
.route-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.time-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.departure-time, .arrival-time {
  font-weight: bold;
  font-size: 16px;
}

.duration {
  width: 100%;
  color: #909399;
  font-size: 12px;
}

.passenger-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.id-card {
  color: #909399;
  font-size: 12px;
}

.seat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.seat-number {
  font-weight: bold;
}

.seat-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.price {
  font-weight: bold;
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.change-dialog-content, .refund-dialog-content {
  padding: 20px 0;
}

.date-selection {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.seat-selection {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.price-difference, .refund-calculation {
  margin: 20px 0;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.price-item {
  font-size: 14px;
}

.change-info {
  margin-top: 20px;
}

.ticket-info {
  margin-bottom: 20px;
}

.success-dialog-content {
  padding: 20px 0;
}
</style>