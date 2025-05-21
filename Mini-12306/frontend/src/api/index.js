import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
  // 查询车次时刻
  querySchedules: async (params = {}) => {
    const { departureStation, arrivalStation, departureDate } = params;
    try {
      const response = await axios.get(`${API_BASE_URL}/schedules/query`, {
        params: { departureStation, arrivalStation, departureDate }
      });
      // 确保返回数组
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      throw error.response?.data || { error: '查询失败，请稍后重试' };
    }
  },
  
  // 购买火车票
  buyTicket: async (purchaseData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/tickets/buy`, purchaseData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: '购票失败，请稍后重试' };
    }
  },
  
  // 退票
  refundTicket: async (refundData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/tickets/refund`, refundData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: '退票失败，请稍后重试' };
    }
  },
  
  // 查询用户车票
  getUserTickets: async (userId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/tickets/user/${userId}`);
      // 确保返回数组
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      throw error.response?.data || { error: '查询用户车票失败' };
    }
  },
  
  // 改签车票
  changeTicket: async (changeData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/tickets/change`, changeData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: '改签失败，请稍后重试' };
    }
  }
};

export default api;
