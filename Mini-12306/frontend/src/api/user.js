import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    console.error('API请求失败:', error);
    return Promise.reject(error);
  }
);

/**
 * 用户相关API
 */
export const userAPI = {
  /**
   * 获取用户信息
   * @param {number} userId 用户ID
   * @returns {Promise}
   */
  async getUserInfo(userId) {
    try {
      const response = await api.get(`/users/${userId}`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '获取用户信息失败'
      };
    }
  },

  /**
   * 更新用户信息
   * @param {Object} userInfo 用户信息
   * @returns {Promise}
   */
  async updateUserInfo(userInfo) {
    try {
      const response = await api.put(`/users/${userInfo.id}`, {
        username: userInfo.username,
        phone: userInfo.phone,
        email: userInfo.email
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '更新用户信息失败'
      };
    }
  },

  /**
   * 修改密码
   * @param {Object} passwordData 密码数据
   * @returns {Promise}
   */
  async changePassword(passwordData) {
    try {
      const response = await api.put(`/users/${passwordData.userId}/password`, {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '修改密码失败'
      };
    }
  },

  /**
   * 验证手机号
   * @param {Object} phoneData 手机验证数据
   * @returns {Promise}
   */
  async verifyPhone(phoneData) {
    try {
      const response = await api.post(`/users/${phoneData.userId}/verify-phone`, {
        phone: phoneData.phone,
        verificationCode: phoneData.verificationCode
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '手机号验证失败'
      };
    }
  },

  /**
   * 发送手机验证码
   * @param {Object} phoneData 手机号数据
   * @returns {Promise}
   */
  async sendPhoneVerificationCode(phoneData) {
    try {
      const response = await api.post(`/users/${phoneData.userId}/send-phone-code`, {
        phone: phoneData.phone
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '发送验证码失败'
      };
    }
  },

  /**
   * 验证邮箱
   * @param {Object} emailData 邮箱验证数据
   * @returns {Promise}
   */
  async verifyEmail(emailData) {
    try {
      const response = await api.post(`/users/${emailData.userId}/verify-email`, {
        email: emailData.email,
        verificationCode: emailData.verificationCode
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '邮箱验证失败'
      };
    }
  },

  /**
   * 发送邮箱验证码
   * @param {Object} emailData 邮箱数据
   * @returns {Promise}
   */
  async sendEmailVerificationCode(emailData) {
    try {
      const response = await api.post(`/users/${emailData.userId}/send-email-code`, {
        email: emailData.email
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '发送验证码失败'
      };
    }
  },

  /**
   * 获取用户登录设备列表
   * @param {number} userId 用户ID
   * @returns {Promise}
   */
  async getUserDevices(userId) {
    try {
      const response = await api.get(`/users/${userId}/devices`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '获取设备列表失败'
      };
    }
  },

  /**
   * 移除登录设备
   * @param {number} userId 用户ID
   * @param {string} deviceId 设备ID
   * @returns {Promise}
   */
  async removeDevice(userId, deviceId) {
    try {
      const response = await api.delete(`/users/${userId}/devices/${deviceId}`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '移除设备失败'
      };
    }
  },

  /**
   * 获取账户安全信息
   * @param {number} userId 用户ID
   * @returns {Promise}
   */
  async getAccountSecurity(userId) {
    try {
      const response = await api.get(`/users/${userId}/security`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '获取安全信息失败'
      };
    }
  }
};

export default userAPI;