<template>
  <div class="passenger-form">
    <el-form 
      ref="passengerFormRef" 
      :model="passengerForm" 
      :rules="passengerRules" 
      label-width="100px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input 
          v-model="passengerForm.name" 
          placeholder="请输入姓名" 
          clearable
        />
      </el-form-item>
      
      <el-form-item label="身份证号" prop="idCard">
        <el-input 
          v-model="passengerForm.idCard" 
          placeholder="请输入身份证号" 
          clearable
        />
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input 
          v-model="passengerForm.phone" 
          placeholder="请输入手机号（可选）" 
          clearable
        />
      </el-form-item>
      
      <el-form-item label="设为默认">
        <el-switch v-model="passengerForm.isDefault" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitPassenger" :loading="submitLoading">
          {{ isEdit ? '更新乘车人' : '添加乘车人' }}
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { passengerApi } from '@/api/index.js'

const { data } = defineProps({
  data: Object,
})

const emit = defineEmits(['action'])

const isEdit = ref(data?.isEdit || false)
const submitLoading = ref(false)
const passengerFormRef = ref(null)

// 表单数据
const passengerForm = reactive({
  name: data?.passenger?.name || '',
  idCard: data?.passenger?.idCard || '',
  phone: data?.passenger?.phone || '',
  isDefault: data?.passenger?.isDefault || false
})

// 表单验证规则
const validateIdCard = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入身份证号'))
  } else if (!/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的18位身份证号'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const passengerRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  idCard: [
    { validator: validateIdCard, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user.id
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  ElMessage.error('请先登录')
  return null
}

// 提交乘车人信息
const submitPassenger = () => {
  passengerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const userId = getCurrentUserId()
        if (!userId) {
          submitLoading.value = false
          return
        }
        const passengerData = {
          ...passengerForm,
          userId
        }
        
        let result
        if (isEdit.value && data?.passenger?.id) {
          result = await passengerApi.updatePassenger(data.passenger.id, passengerData)
        } else {
          result = await passengerApi.addPassenger(passengerData)
        }
        
        if (result.success) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          emit('action', { 
            type: isEdit.value ? 'update' : 'add', 
            data: result.data,
            message: isEdit.value ? '乘车人信息已更新' : '乘车人添加成功'
          })
          
          if (!isEdit.value) {
            resetForm()
          }
        } else {
          ElMessage.error(result.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (passengerFormRef.value) {
    passengerFormRef.value.resetFields()
  }
  Object.assign(passengerForm, {
    name: '',
    idCard: '',
    phone: '',
    isDefault: false
  })
}
</script>

<style scoped>
.passenger-form {
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
</style>