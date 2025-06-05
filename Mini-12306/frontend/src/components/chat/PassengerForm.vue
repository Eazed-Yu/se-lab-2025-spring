<template>
  <div class="passenger-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入乘客姓名" />
      </el-form-item>
      
      <el-form-item label="证件类型" prop="idType">
        <el-select v-model="form.idType" placeholder="请选择证件类型">
          <el-option label="身份证" value="ID_CARD" />
          <el-option label="护照" value="PASSPORT" />
          <el-option label="港澳通行证" value="HK_MACAO_PASS" />
          <el-option label="台湾通行证" value="TAIWAN_PASS" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="证件号码" prop="idNumber">
        <el-input v-model="form.idNumber" placeholder="请输入证件号码" />
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      
      <el-form-item label="乘客类型" prop="passengerType">
        <el-select v-model="form.passengerType" placeholder="请选择乘客类型">
          <el-option label="成人" value="ADULT" />
          <el-option label="儿童" value="CHILD" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">保存乘客信息</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['action']);

const formRef = ref();
const form = reactive({
  name: '',
  idType: 'ID_CARD',
  idNumber: '',
  phone: '',
  passengerType: 'ADULT'
});

const rules = {
  name: [
    { required: true, message: '请输入乘客姓名', trigger: 'blur' }
  ],
  idType: [
    { required: true, message: '请选择证件类型', trigger: 'change' }
  ],
  idNumber: [
    { required: true, message: '请输入证件号码', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  passengerType: [
    { required: true, message: '请选择乘客类型', trigger: 'change' }
  ]
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    emit('action', {
      type: 'save_passenger',
      data: { ...form }
    });
    ElMessage.success('乘客信息保存成功');
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

const handleCancel = () => {
  emit('action', {
    type: 'cancel_passenger_form'
  });
};
</script>

<style scoped>
.passenger-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.el-form-item {
  margin-bottom: 20px;
}

.el-button {
  margin-right: 10px;
}
</style>