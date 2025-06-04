import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// 创建axios实例
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
});

// 响应拦截器
axiosInstance.interceptors.response.use(
  response => {
    // 如果响应是标准的ApiResponse格式，则处理响应
    if (response.data && (response.data.hasOwnProperty('success') || response.data.hasOwnProperty('code'))) {
      // 检查是否使用success字段（后端标准格式）
      if (response.data.hasOwnProperty('success')) {
        if (response.data.success) {
          // 成功响应，返回数据部分
          return response.data;
        } else {
          // 业务逻辑错误，抛出错误信息
          return Promise.reject({
            message: response.data.message || '操作失败',
            data: response.data.data
          });
        }
      } 
      // 兼容code字段格式
      else if (response.data.code === 200) {
        return response.data;
      } else {
        return Promise.reject({
          message: response.data.message || '操作失败',
          data: response.data.data
        });
      }
    }
    return response.data;
  },
  error => {
    return Promise.reject({
      message: error.response?.data?.message || '网络错误，请稍后重试',
      status: error.response?.status
    });
  }
);

// 用户相关API
export const userApi = {
  // 用户登录
  login: (loginData) => {
    return axiosInstance.post('/users/login', loginData);
  },
  
  // 用户注册
  register: (registerData) => {
    return axiosInstance.post('/users/register', registerData);
  },
  
  // 获取用户信息
  getUserInfo: (userId) => {
    return axiosInstance.get(`/users/${userId}`);
  }
};

// 车票相关API
export const ticketApi = {
  // 查询车次时刻
  querySchedules: (params) => {
    return axiosInstance.get('/schedules/query', { params });
  },
  
  // 购买火车票
  buyTicket: (purchaseData) => {
    return axiosInstance.post('/tickets/buy', purchaseData);
  },
  
  // 查询用户车票
  getUserTickets: (userId) => {
    return axiosInstance.get(`/tickets/user/${userId}`);
  },
  
  // 改签车票
  changeTicket: (changeData) => {
    return axiosInstance.post('/tickets/change', changeData);
  },
  
  // 退票
  refundTicket: (refundData) => {
    return axiosInstance.post('/tickets/refund', refundData);
  }
};

// 订单相关API
export const orderApi = {
  // 查询用户订单
  getUserOrders: (userId) => {
    return axiosInstance.get(`/orders/user/${userId}`);
  },
  
  // 取消订单
  cancelOrder: (orderId) => {
    return axiosInstance.post(`/orders/${orderId}/cancel`);
  },
  
  // 根据订单ID查询订单
  getOrderById: (orderId) => {
    return axiosInstance.get(`/orders/${orderId}`);
  }
};

// 乘车人相关API
export const passengerApi = {
  // 获取用户乘车人列表
  getPassengerList: (userId) => {
    return axiosInstance.get('/passengers', { params: { userId } });
  },
  
  // 添加乘车人
  addPassenger: (passengerData) => {
    return axiosInstance.post('/passengers', passengerData, {
      params: { userId: passengerData.userId }
    });
  },
  
  // 根据ID获取乘车人
  getPassengerById: (passengerId) => {
    return axiosInstance.get(`/passengers/${passengerId}`);
  },
  
  // 更新乘车人信息
  updatePassenger: (passengerId, passengerData) => {
    return axiosInstance.put(`/passengers/${passengerId}`, passengerData);
  },
  
  // 删除乘车人
  deletePassenger: (passengerId) => {
    return axiosInstance.delete(`/passengers/${passengerId}`);
  },
  
  // 设置默认乘车人
  setDefaultPassenger: (userId, passengerId) => {
    return axiosInstance.put(`/passengers/${passengerId}/default`, null, {
      params: { userId }
    });
  },
  
  // 获取默认乘车人
  getDefaultPassenger: (userId) => {
    return axiosInstance.get('/passengers/default', { params: { userId } });
  },
  
  // 上传身份证照片
  uploadIdCardPhoto: (passengerId, formData) => {
    return axiosInstance.post(`/passengers/${passengerId}/id-card-photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
};

// 导出所有API
export const api = {
  ...userApi,
  ...ticketApi,
  ...orderApi,
  ...passengerApi
};

export default api;
