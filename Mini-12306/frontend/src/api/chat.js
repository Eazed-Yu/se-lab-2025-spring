import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 聊天API服务
 */
export const chatAPI = {
  /**
   * 创建新的聊天会话
   */
  async createSession(userId) {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/chat/sessions`, null, {
        params: { userId }
      });
      return response.data;
    } catch (error) {
      console.error('创建聊天会话失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户的所有聊天会话
   */
  async getUserSessions(userId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/chat/sessions`, {
        params: { userId }
      });
      return response.data;
    } catch (error) {
      console.error('获取用户聊天会话失败:', error);
      throw error;
    }
  },

  /**
   * 获取指定聊天会话详情
   */
  async getSession(sessionId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/chat/sessions/${sessionId}`);
      return response.data;
    } catch (error) {
      console.error('获取聊天会话详情失败:', error);
      throw error;
    }
  },

  /**
   * 更新聊天会话标题
   */
  async updateSessionTitle(sessionId, title) {
    try {
      const response = await axios.put(`${API_BASE_URL}/api/chat/sessions/${sessionId}/title`, {
        title
      });
      return response.data;
    } catch (error) {
      console.error('更新聊天会话标题失败:', error);
      throw error;
    }
  },

  /**
   * 删除聊天会话
   */
  async deleteSession(sessionId) {
    try {
      const response = await axios.delete(`${API_BASE_URL}/api/chat/sessions/${sessionId}`);
      return response.data;
    } catch (error) {
      console.error('删除聊天会话失败:', error);
      throw error;
    }
  },

  /**
   * 清理非活跃会话
   */
  async cleanupInactiveSessions() {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/chat/cleanup`);
      return response.data;
    } catch (error) {
      console.error('清理非活跃会话失败:', error);
      throw error;
    }
  }
};

export default chatAPI;